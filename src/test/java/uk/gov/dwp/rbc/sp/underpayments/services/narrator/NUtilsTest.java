package uk.gov.dwp.rbc.sp.underpayments.services.narrator;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class NUtilsTest {

    @Test
    void toJson() {

        val dt = LocalDate.now();
        val u = new NUtils();
        System.out.println(u.toJson(dt));

    }
}