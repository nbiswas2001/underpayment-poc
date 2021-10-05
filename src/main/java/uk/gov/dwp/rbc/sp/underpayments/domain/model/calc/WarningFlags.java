package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import uk.gov.dwp.rbc.sp.underpayments.utils.AbstractFlags;

public class WarningFlags extends AbstractFlags<WarningFlags.Warning>  {

    //-------------------------------
    public WarningFlags() {
        this(0);
    }

    @Override
    protected Warning[] values() {
        return Warning.values();
    }

    public WarningFlags(Integer data) {
        super(data);
    }

    //----------------------
    public enum Warning {
        //On Account
        PRSN_COUNTRY_CODE_MISSING,  // 0
        PRSN_ADDRESS_MISSING,       // 1
        PRSN_ADDRESS_MALFORMED,     // 2
        PRSN_NAME_MALFORMED,        // 3
        PRSN_FIRST_NAME_MISSING,    // 4
        PRSN_SURNAME_MISSING,       // 5
        PRSN_UNVERIFIED_DOB,        // 6
        PRSN_UNVERIFIED_DOD,        // 7
        PRSNTOPRSN_DUPLICATES,      // 8
        AC_PRE_PSCS_SPA_DATE,       // 9
        AC_INTERNATIONAL,           // 10
        AC_DUMMY_3,                 // 11 - Placeholder
        AC_DUMMY_4,                 // 12 - Placeholder

        //On Relationship
        RELN_DUMMY_1,               // 13 - Placeholder
        RELN_DUMMY_2,               // 14 - Placeholder
        RELN_DUMMY_3,               // 15 - Placeholder

        //On SpAward
        AWCM_SAC_BYTE_COUNT_MISMATCH, // 16
        AWCM_INVALID_START_DATE       // 17
    }
}
