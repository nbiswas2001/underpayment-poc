package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.WarningFlags;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void test() {
        val account = new Account();

        account.tryLoad(acProblems -> {
            acProblems.errors().set(ErrorFlags.Error.AC_AWARD_HAS_ERRORS);
        });

        assertEquals(true, account.getProblems().getHasErrors());
        assertEquals(false, account.getProblems().getHasWarnings());
        assertEquals(true, account.getProblems().errors().isSet(ErrorFlags.Error.AC_AWARD_HAS_ERRORS));
        assertEquals(false, account.getProblems().errors().isSet(ErrorFlags.Error.PRSN_INVALID_SEX));
        assertEquals(16, account.getProblems().getErrorFlagsData());
        assertEquals(0, account.getProblems().getWarningFlagsData());

        account.tryLoad(acProblems -> {
            acProblems.warnings().set(WarningFlags.Warning.PRSN_ADDRESS_MISSING);
        });

        assertEquals(true, account.getProblems().getHasErrors());
        assertEquals(true, account.getProblems().getHasWarnings());
        assertEquals(true, account.getProblems().errors().isSet(ErrorFlags.Error.AC_AWARD_HAS_ERRORS));
        assertEquals(true, account.getProblems().warnings().isSet(WarningFlags.Warning.PRSN_ADDRESS_MISSING));
        assertEquals(16, account.getProblems().getErrorFlagsData());
        assertEquals(2, account.getProblems().getWarningFlagsData());
    }

}