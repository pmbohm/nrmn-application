package au.org.aodn.nrmn.restapi.validation.validators.formatted;

import au.org.aodn.nrmn.restapi.model.db.StagedRowError;
import au.org.aodn.nrmn.restapi.model.db.composedID.ErrorID;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationCategory;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationLevel;
import au.org.aodn.nrmn.restapi.validation.BaseFormattedValidator;
import au.org.aodn.nrmn.restapi.validation.StagedRowFormatted;
import cyclops.control.Validated;
import lombok.val;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Method3QuadratMax50 extends BaseFormattedValidator {
    public Method3QuadratMax50() {
        super("measure");
    }

    @Override
    public Validated<StagedRowError, String> valid(StagedRowFormatted target) {
        if (!target.getMethod().equals(3)) {
            return Validated.valid("not affected");
        }
        val isUnder50 = IntStream.range(1, 5)
                .map(i -> target.getMeasureJson().getOrDefault(i, 0))
                .allMatch(measure -> measure <= 50);
        if (isUnder50)
            return Validated.valid("quadrats under 50");
        return Validated.invalid(new StagedRowError(
                new ErrorID(target.getId(),
                        target.getRef().getStagedJob().getId(),
                        "Quadrats above 50"),
                ValidationCategory.DATA,
                ValidationLevel.BLOCKING,
                columnTarget,
                target.getRef()));
    }
}