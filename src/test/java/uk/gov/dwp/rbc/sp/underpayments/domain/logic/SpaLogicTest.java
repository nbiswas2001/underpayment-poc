package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Sex;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

class SpaLogicTest {

    SpaLogic logic = new SpaLogic();

    @Test
    void getAgeCategory() {
    }

    @Test
    void getDateOfSpa() {
        val spaDate = logic.getDateOfSpa(Sex.F, DateUtils.dt(6,4,1950));
        System.out.println("SPA Date: "+(s(spaDate)));
    }

    //------------------------------------------------------------------------
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private String s(OffsetDateTime dt) {
        return formatter.format(dt);
    }
}