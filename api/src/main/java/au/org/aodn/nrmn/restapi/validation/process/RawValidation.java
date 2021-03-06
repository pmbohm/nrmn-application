package au.org.aodn.nrmn.restapi.validation.process;

import au.org.aodn.nrmn.restapi.model.db.*;
import au.org.aodn.nrmn.restapi.model.db.enums.Directions;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationLevel;
import au.org.aodn.nrmn.restapi.repository.DiverRepository;
import au.org.aodn.nrmn.restapi.repository.ObservationRepository;
import au.org.aodn.nrmn.restapi.util.OptionalUtil;
import au.org.aodn.nrmn.restapi.util.ValidatorHelpers;
import au.org.aodn.nrmn.restapi.validation.StagedRowFormatted;
import au.org.aodn.nrmn.restapi.validation.model.RowWithValidation;
import au.org.aodn.nrmn.restapi.validation.provider.ATRCValidators;
import au.org.aodn.nrmn.restapi.validation.provider.RLSValidators;
import au.org.aodn.nrmn.restapi.validation.validators.base.BaseRowValidator;
import au.org.aodn.nrmn.restapi.validation.validators.row.data.DirectionDataCheck;
import au.org.aodn.nrmn.restapi.validation.validators.row.data.SpeciesNotFoundCheck;
import au.org.aodn.nrmn.restapi.validation.validators.row.entities.DiverExists;
import au.org.aodn.nrmn.restapi.validation.validators.row.entities.ObservableItemExists;
import au.org.aodn.nrmn.restapi.validation.validators.row.entities.SiteCodeExists;
import au.org.aodn.nrmn.restapi.validation.validators.row.passThu.PassThruRef;
import au.org.aodn.nrmn.restapi.validation.validators.row.passThu.PassThruString;
import au.org.aodn.nrmn.restapi.validation.validators.row.format.*;
import cyclops.companion.Monoids;
import cyclops.control.Validated;
import cyclops.data.HashMap;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RawValidation extends ValidatorHelpers {
    @Autowired
    DiverRepository diverRepo;
    @Autowired
    SiteCodeExists siteCodeExists;

    @Autowired
    ObservableItemExists observableItemExists;

    @Autowired
    RLSValidators rlsValidators;

    @Autowired
    ATRCValidators atrcValidators;

    @Autowired
    ObservationRepository obsRepo;


    public HashMap<String, BaseRowValidator> getExtendedValidators() {
        return HashMap.fromStream(
                Stream.of(
                        Tuple2.of("IsInvertSizing", new OptionalBooleanFormatValidation(target -> {
                            val invertSizing = Optional.ofNullable(target.getIsInvertSizing()).orElseGet(() -> "false");
                            return String.valueOf(invertSizing.equalsIgnoreCase("yes"));
                        },
                                "IsInvertSizing"))

                )
        );
    }


    public Seq<Tuple2<String, BaseRowValidator>> getRowValidators() {
        return
                Seq.of(
                        Tuple2.of("Site", siteCodeExists),
                        Tuple2.of("Date", new DateFormatValidation()),
                        Tuple2.of("Time", new TimeFormatValidation()),
                        Tuple2.of("Diver", new DiverExists(StagedRow::getDiver, "Diver", diverRepo, ValidationLevel.BLOCKING)),
                        Tuple2.of("Buddy", new DiverExists(StagedRow::getBuddy, "Buddy", diverRepo, ValidationLevel.WARNING)),
                        Tuple2.of("P-Qs", new DiverExists(StagedRow::getPqs, "P-Qs", diverRepo, ValidationLevel.BLOCKING)),
                        Tuple2.of("Block", new IntegerFormatValidation(StagedRow::getBlock, "Block", Arrays.asList(0, 1, 2, 10))),
                        Tuple2.of("Code", new PassThruString(StagedRow::getCode, "Code")),
                        Tuple2.of("Species", observableItemExists),

                        Tuple2.of("Vis", new OptionalPositiveIntegerFormatValidation(StagedRow::getVis, "Vis")),

                        Tuple2.of("Total", new IntegerFormatValidation(StagedRow::getTotal, "Total", Collections.emptyList())),
                        Tuple2.of("Inverts", new OptionalIntegerFormatValidation(StagedRow::getInverts, "Inverts")),
                        Tuple2.of("MeasureJson", new MeasureJsonValidation()),
                        Tuple2.of("Latitude", new DoubleFormatValidation(StagedRow::getLatitude, "Latitude")),
                        Tuple2.of("Longitude", new DoubleFormatValidation(StagedRow::getLongitude, "Longitude")),
                        Tuple2.of("Direction", new DirectionDataCheck()),
                        Tuple2.of("SpeciesNotfound", new SpeciesNotFoundCheck()),
                        Tuple2.of("Ref", new PassThruRef())
                );
    }

    public Seq<Tuple2<String, BaseRowValidator>> getRawValidators(StagedJob job) {
        val program = job.getProgram();

        val extendedChecks = job.getIsExtendedSize() ?
                getExtendedValidators() :
                HashMap.<String, BaseRowValidator>empty();

        Seq<Tuple2<String, BaseRowValidator>> programChecks = program.getProgramName().equals("RLS") ?
                rlsValidators.getRowValidators() :
                atrcValidators.getRowValidators();

        return getRowValidators().appendAll(programChecks).appendAll(extendedChecks);
    }


    public RowWithValidation<Seq<Tuple2<String, Object>>> validate(StagedRow target,
                                                                   Seq<Tuple2<String, BaseRowValidator>> validators) {
        val validation = validators.map(tuple ->
                tuple._2().valid(target)
                        .bimap(Function.identity(),
                                content -> Seq.of(Tuple2.of(tuple._1(), content)))
        ).stream().reduce(
                Validated.valid(Seq.empty()),
                (v1, v2) -> v1.combine(Monoids.seqConcat(), v2)
        );
        val errors = toErrorList(validation);
        target.setErrors(errors);
        return new RowWithValidation(Seq.of(target), validation);
    }

    public Optional<StagedRowFormatted> toFormat(HashMap<String, Object> values, boolean isExtendedSizing) {
        val species = (ObservableItem) values.get("Species").orElseGet(null);
        if (species == null)
            return Optional.empty();

        val site = (Site) values.get("Site").orElseGet(null);
        val date = (LocalDate) values.get("Date").orElseGet(null);
        val time = (Optional<LocalTime>) values.get("Time").orElse(Optional.empty());

        val diver = (Diver) values.get("Diver").orElseGet(null);
        val longitude = (Double) values.get("Longitude").orElseGet(null);
        val latitude = (Double) values.get("Latitude").orElseGet(null);

        val splitDepth = values.get("Depth").orElseGet(null).toString().split("\\.");
        val depth = Integer.parseInt(splitDepth[0]);

        Optional<Integer> survey_num = splitDepth.length == 1
                ? Optional.empty()
                : Optional.of(Integer.parseInt(splitDepth[1]));

        val method = (Integer) values.get("Method").orElseGet(null);
        val block = (Integer) values.get("Block").orElseGet(null);
        val speciesAttributesOtp = obsRepo.getSpeciesAttributesById(new Long(species.getObservableItemId()));
        val mayBeSpeciesAttributes = speciesAttributesOtp
                .stream()
                .findFirst();
        val code = (String) values.get("Code").orElseGet(null);

        val vis = (Optional<Integer>) values.get("Vis").orElse(Optional.empty());
        val total = (Integer) values.get("Total").orElseGet(null);
        val inverts = (Optional<Integer>) values.get("Inverts").orElse(Optional.empty());
        val direction = (Directions) values.get("Direction").orElseGet(null);
        val measureJson = (java.util.Map<Integer, Integer>) values.get("MeasureJson").orElseGet(null);

        val ref = (StagedRow) values.get("Ref").orElseGet(null);

        val rowFormatted = new StagedRowFormatted();
        rowFormatted.setId(ref.getId());
        rowFormatted.setDate(date);
        rowFormatted.setTime(time);
        rowFormatted.setSite(site);
        rowFormatted.setDiver(diver);
        rowFormatted.setLongitude(longitude);
        rowFormatted.setLatitude(latitude);
        rowFormatted.setDepth(depth);
        rowFormatted.setSurveyNum(survey_num);
        rowFormatted.setMethod(method);
        rowFormatted.setBlock(block);
        rowFormatted.setSpecies(species);
        rowFormatted.setVis(vis);
        rowFormatted.setCode(code);
        rowFormatted.setDirection(direction);
        rowFormatted.setTotal(total);
        rowFormatted.setInverts(inverts.orElse(0));
        rowFormatted.setMeasureJson(measureJson);
        rowFormatted.setRef(ref);
        rowFormatted.setSpeciesAttributesOpt(mayBeSpeciesAttributes);

        if (isExtendedSizing) {
            val isInvertSizing = (Optional<Boolean>) values.get("IsInvertSizing").orElse(Optional.empty());
            rowFormatted.setIsInvertSizing(isInvertSizing);
        }
        return Optional.of(rowFormatted);
    }

    public List<StagedRowFormatted> preValidated(List<StagedRow> targets, StagedJob job) {

        val validators = getRawValidators(job);

        return targets
                .stream()
                .flatMap(row -> {
                    val validatedRow = validate(row, validators);
                    val validated = validatedRow.getValid();
                    val validatorsWithMap =
                            validated.map(seq -> toFormat(seq.toHashMap(Tuple2::_1, Tuple2::_2), job.getIsExtendedSize()));
                    return validatorsWithMap.stream().flatMap(OptionalUtil::toStream);
                }).collect(Collectors.toList());
    }
}

