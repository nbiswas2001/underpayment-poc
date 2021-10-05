package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.val;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DateUtils {

    //------------------------------------------------------------------------------------------
    public static boolean isDateBetween(OffsetDateTime date, OffsetDateTime startDate, OffsetDateTime endDate){

        if(startDate == null) throw new UeException("Start date is null");
        return  date!=null &&
                (
                    ( date.isEqual(startDate) || date.isAfter(startDate) )
                    &&
                    ( endDate == null || date.isEqual(endDate) || date.isBefore(endDate) )
                );
    }
    //---------------------------------------------------------------------------
    public static boolean isDateAfter(OffsetDateTime date, OffsetDateTime refDate){
        if(refDate == null) return false;
        else return date==null || (date!=null && date.isAfter(refDate));
    }
    //---------------------------------------------------------------------------
    public static boolean isDateOnOrAfter(OffsetDateTime date, OffsetDateTime refDate){
        if(date == null && refDate == null) throw new UeException("Cannot compare dates as both are null");
        return  isDateAfter(date, refDate) || ( date != null && date.isEqual(refDate) ) ;
    }

    //---------------------------------------------------------------------------
    public static boolean isDateOnOrBefore(OffsetDateTime date, OffsetDateTime refDate){
        return !isDateAfter(date, refDate);
    }
    //---------------------------------------------------------------------------
    public static boolean isDateBefore(OffsetDateTime date, OffsetDateTime refDate){
        if(date == null) return false;
        else return refDate==null || (date != null && date.isBefore(refDate));
    }

    //-----------------------------------------------------------------
    public static OffsetDateTime earliestOf(OffsetDateTime ... dates) {
        val nonNullDates = Arrays.stream(dates).filter(dt -> dt != null).collect(Collectors.toList());
        if(nonNullDates.isEmpty()) return null;
        else return nonNullDates.stream().sorted(OffsetDateTime::compareTo).findFirst().get();
    }

    //-----------------------------------------------------------------
    public static OffsetDateTime dt(int d, int m, int y){
        return OffsetDateTime.of(y, m, d,0,0,0,0, ZoneOffset.UTC);
    }

}
