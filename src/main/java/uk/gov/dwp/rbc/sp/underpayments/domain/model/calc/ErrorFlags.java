package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import uk.gov.dwp.rbc.sp.underpayments.utils.AbstractFlags;

public class ErrorFlags extends AbstractFlags<ErrorFlags.Error>  {

    //-------------------------------
    public ErrorFlags() {this(0);}
    public ErrorFlags(Integer data) {
        super(data);
    }

    @Override
    protected Error[] values() {
        return Error.values();
    }

    //----------------------
    public enum Error {

        //On Account
        PRSN_INVALID_SEX,                   // 0
        AC_RELN_HAS_ERRORS,                 // 1
        AC_AWARD_HAS_ERRORS,                // 2
        EXCEPTION_CALC_AC_ELIGIBILITY,      // 3
        AC_DUMMY_0,                         // 4 - Placeholder
        EXCEPTION_CALC_CIRCS_ELIGIBILITY,   // 5
        EXCEPTION_CALC_ENTITLEMENT,         // 6
        MORE_THAN_1_PRLBN,                  // 7
        PRSNTOPRSN_HAS_DATE_PROBLEMS,       // 8
        AC_DUMMY_1,                         // 9 - Placeholder
        AC_DUMMY_2,                         // 10 - Placeholder
        AC_DUMMY_3,                         // 11 - Placeholder
        AC_DUMMY_4,                         // 12 - Placeholder
        AC_DUMMY_5,                         // 13 - Placeholder
        AC_DUMMY_6,                         // 14 - Placeholder

        //On Relationship
        PRSNTOPRSN_SPOUSE_NINO_MISSING,     // 15
        RELN_SPOUSE_AC_MISSING,             // 16
        RELN_DUMMY_1,                       // 17 - Placeholder
        RELN_DUMMY_2,                       // 18 - Placeholder
        RELN_DUMMY_3,                       // 19 - Placeholder

        //On SpAward
        AWCM_NO_SACS,                       // 20
        AW_NO_START_DATE,                   // 21
        AW_DUMMY_2,                         // 22 - Placeholder
        AW_DUMMY_3,                         // 23 - Placeholder
        AW_DUMMY_4,                         // 24 - Placeholder

        //On Account/Relationship/SpAward
        DATA_LOAD_EXCEPTION                 // 25

    }
}
