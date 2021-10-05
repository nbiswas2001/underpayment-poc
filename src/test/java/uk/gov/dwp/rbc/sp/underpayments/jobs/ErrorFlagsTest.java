package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.*;

class ErrorFlagsTest {

    @Test
    void test() {
        val ef = new ErrorFlags();
        //ef.set(PRSN_COUNTRY_CODE_MISSING);
        //ef.set(RELN_SPOUSE_AC_MISSING);
        //ef.set(RELN_SPOUSE_AC_MISSING);
        //ef.set(PRSN_UNVERIFIED_DOD);
        ef.set(AC_AWARD_HAS_ERRORS);

        val x = ef.getData();
        //assertEquals(0, x);


    }

}