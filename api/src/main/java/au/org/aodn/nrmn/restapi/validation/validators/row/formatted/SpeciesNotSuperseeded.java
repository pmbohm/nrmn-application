package au.org.aodn.nrmn.restapi.validation.validators.row.formatted;

import au.org.aodn.nrmn.restapi.model.db.StagedRowError;
import au.org.aodn.nrmn.restapi.model.db.composedID.ErrorID;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationCategory;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationLevel;
import au.org.aodn.nrmn.restapi.validation.validators.base.BaseFormattedValidator;
import au.org.aodn.nrmn.restapi.validation.StagedRowFormatted;
import cyclops.control.Validated;

public class SpeciesNotSuperseeded extends BaseFormattedValidator {
    public SpeciesNotSuperseeded() {
        super("Species");
    }

    @Override
    public Validated<StagedRowError, String> valid(StagedRowFormatted target) {
                if (target.getSpecies().getSupersededBy() == null) {
                    return Validated.valid("Species Not Superseded");
                }
        return Validated.invalid(new StagedRowError(
                new ErrorID(target.getId(),
                        target.getRef().getStagedJob().getId(),
                        target.getRef().getSpecies() + " is superseded"),
                ValidationCategory.DATA,
                ValidationLevel.WARNING,
                columnTarget,
                target.getRef()));
    }
}
