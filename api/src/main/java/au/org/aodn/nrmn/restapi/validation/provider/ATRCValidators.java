package au.org.aodn.nrmn.restapi.validation.provider;

import au.org.aodn.nrmn.restapi.model.db.StagedRow;
import au.org.aodn.nrmn.restapi.validation.validators.base.BaseFormattedValidator;
import au.org.aodn.nrmn.restapi.validation.validators.base.BaseGlobalRawValidator;
import au.org.aodn.nrmn.restapi.validation.validators.base.BaseRowValidator;
import au.org.aodn.nrmn.restapi.validation.validators.row.data.ATRCMethod7BlockCheck;
import au.org.aodn.nrmn.restapi.validation.validators.row.format.ATRCDepthValidation;
import au.org.aodn.nrmn.restapi.validation.validators.row.formatted.Method3QuadratMax50;
import au.org.aodn.nrmn.restapi.validation.validators.row.formatted.TooOldFutureDate;
import au.org.aodn.nrmn.restapi.validation.validators.row.format.IntegerFormatValidation;
import au.org.aodn.nrmn.restapi.validation.validators.global.raw.ATRCMethodCheck;
import au.org.aodn.nrmn.restapi.validation.validators.global.raw.ATRCSurveyGroupComplete;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("ATRC")
public class ATRCValidators implements ValidatorProvider {


    @Autowired
    ATRCMethodCheck atrcMethodCheck;

    @Autowired
    ATRCSurveyGroupComplete atrcSurveyGroupComplete;

    @Override
    public Seq<Tuple2<String, BaseRowValidator>> getRowValidators() {
        return Seq.of(
                Tuple2.of("Depth", new ATRCDepthValidation()),
                Tuple2.of("Method", new IntegerFormatValidation(StagedRow::getMethod, "Method",
                        Arrays.asList(0, 1, 2, 3, 4, 5, 7, 10))),
                Tuple2.of("Method7Block", new ATRCMethod7BlockCheck())
        );
    }

    @Override
    public Seq<BaseFormattedValidator> getFormattedValidators() {

        return Seq.of(
                new TooOldFutureDate("1992-01-01"),
                new  Method3QuadratMax50()
        );
    }

    @Override
    public Seq<BaseGlobalRawValidator> getGlobalValidators() {
        return Seq.of(
            atrcMethodCheck,
            atrcSurveyGroupComplete
        );
    }
}



