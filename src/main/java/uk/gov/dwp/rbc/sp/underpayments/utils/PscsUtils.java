package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.val;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class PscsUtils {

    //-----------------------------------------------
    private static OffsetDateTime baseDate = DateUtils.dt(28,2,1852);
    public static OffsetDateTime toDate(Integer pscsDate) {
        return pscsDate > 0? baseDate.plusDays(pscsDate) : null;
    }

    //-----------------------------------------------
    public static int hexToInt(String hexData){
        try {
            return new BigInteger(Hex.decodeHex(hexData)).intValue();
        } catch (DecoderException e) {
            throw new UeException("Failed to decode hex data to int");
        }
    }

    //----------------------------------------------
    public static int intData(String hexData, int from, int length){
        if(hexData == null || hexData.length() < (from+length)*2) return -1;
        else return hexToInt(hexData.substring(from*2, (from+length)*2));
    }

    //-----------------------------------------------
    public static String hexToString(String hexData){
        String result = null;
        try {
            val bytes = Hex.decodeHex(hexData);
            result = new String(bytes, StandardCharsets.US_ASCII);
        } catch (DecoderException e) {
            throw new UeException("Failed to decode hex data to int");
        }
        return result;
    }

}
