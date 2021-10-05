package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void isDateBetween() {
        assertTrue(DateUtils.isDateBetween(dt(1,3,2020), dt(1,1,2020), dt(1,5,2020)));
        assertTrue(DateUtils.isDateBetween(dt(1,3,2020), dt(1,3,2020), dt(1,5,2020)));
        assertTrue(DateUtils.isDateBetween(dt(1,5,2020), dt(1,1,2020), dt(1,5,2020)));
        assertFalse(DateUtils.isDateBetween(null, dt(1,1,2020), dt(1,5,2020)));
        assertTrue(DateUtils.isDateBetween(dt(1,5,2020), dt(1,1,2020), null));
        assertThrows(UeException.class, () ->  DateUtils.isDateBetween(dt(1,5,2020), null, null));
    }

    @Test
    void isDateAfter() {
        assertTrue(DateUtils.isDateAfter(dt(1,3,2020), dt(1,2,2020)));
        assertTrue(DateUtils.isDateAfter(null, dt(1,2,2020)));
        assertFalse(DateUtils.isDateAfter(dt(1,3,2020), null));
        assertFalse(DateUtils.isDateAfter(null, null));
        assertFalse(DateUtils.isDateAfter(dt(1,2,2020), dt(1,2,2020)));
    }

    @Test
    void isDateOnOrAfter() {
        assertTrue(DateUtils.isDateOnOrAfter(dt(1,2,2020), dt(1,2,2020)));
        assertTrue(DateUtils.isDateOnOrAfter(dt(1,3,2020), dt(1,2,2020)));
        assertFalse(DateUtils.isDateOnOrAfter(dt(1,3,2020), dt(1,2,2021)));
    }

    @Test
    void isDateOnOrBefore() {
        assertTrue(DateUtils.isDateOnOrBefore(dt(1,2,2020), dt(1,2,2020)));
        assertFalse(DateUtils.isDateOnOrBefore(dt(1,3,2020), dt(1,2,2020)));
        assertTrue(DateUtils.isDateOnOrBefore(dt(1,3,2020), dt(1,2,2021)));
    }


    @Test
    void testIsDateBetween() {
    }

    @Test
    void testIsDateAfter() {
    }

    @Test
    void testIsDateOnOrAfter() {
    }

    @Test
    void testIsDateOnOrBefore() {
    }

    @Test
    void isDateBefore() {
    }

    @Test
    void getActiveOnDate() {
    }

    @Test
    void earliestOf() {
        val dt1 = dt(3,2,2001);
        val dt2 = dt(7,5,1991);
        val dt3 = dt(1,2,2011);
        OffsetDateTime dtNull = null;

        assertEquals(dt(7,5,1991), DateUtils.earliestOf(dt1,dt2,dt3));

        assertEquals(dt(3,2,2001), DateUtils.earliestOf(dt1,dtNull,dtNull));

        assertNull(DateUtils.earliestOf(dtNull,dtNull));

    }

    private OffsetDateTime dt(int d, int m, int y){
        return DateUtils.dt(d,m,y);
    }

}