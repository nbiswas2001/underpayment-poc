package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PscsUtilsTest {

    @Test
    void toDate() {
    }

    @Test
    void hexToInt() {
    }

    @Test
    void intData() {
    }

    @Test
    void hexToString() {

        var data = "00010100271000000000000000002020202020202020";
        assertEquals(0, PscsUtils.intData(data,3,1));

        data = "00010109191E00000000000000002020202020202020";
        assertEquals(25, PscsUtils.intData(data,4,1));
        assertEquals(30, PscsUtils.intData(data,5,1));

    }
}