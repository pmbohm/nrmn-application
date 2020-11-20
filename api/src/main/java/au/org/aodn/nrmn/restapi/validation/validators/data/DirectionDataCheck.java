package au.org.aodn.nrmn.restapi.validation.validators.data;

import au.org.aodn.nrmn.restapi.model.db.StagedRowError;
import au.org.aodn.nrmn.restapi.model.db.StagedRow;
import au.org.aodn.nrmn.restapi.model.db.composedID.ErrorID;
import au.org.aodn.nrmn.restapi.model.db.enums.Directions;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationCategory;
import au.org.aodn.nrmn.restapi.validation.BaseRowValidator;
import cyclops.control.Validated;
import org.apache.commons.lang3.EnumUtils;

public class DirectionDataCheck extends BaseRowValidator {
    public DirectionDataCheck() {
        super("Direction");
    }

    @Override
    public Validated<StagedRowError, Directions> valid(StagedRow target) {
        if (EnumUtils.isValidEnum(Directions.class, target.getDirection()))
            return Validated.valid(Directions.valueOf(target.getDirection()));
        return Validated.invalid(
                new StagedRowError(
                        new ErrorID(target.getId(),
                                target.getStagedJob().getId(),
                                columnTarget + "format should be a valid direction: N,NE,E,SE,S,SW,W,NW"),
                        ValidationCategory.DATA,
                        columnTarget,
                        target));
    }
}