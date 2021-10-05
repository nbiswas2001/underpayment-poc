package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Sex.F;

class DoBRulesTest {

    SpaLogic rules = new SpaLogic();

    @Test
    void getAgeCategory() {
    }

    @Test
    void getDateOfSpa() {
        val d = rules.getDateOfSpa(F, DateUtils.dt(1951,1,4));
        assertEquals(DateUtils.dt(2011,9,6), d);
    }

}