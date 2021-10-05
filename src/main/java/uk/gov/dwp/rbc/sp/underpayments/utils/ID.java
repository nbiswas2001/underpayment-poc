package uk.gov.dwp.rbc.sp.underpayments.utils;

public class ID {
    public static Long from(String s){
        if(s==null || s.isEmpty()) return null;
        else return Long.parseLong(s);
    }

    public static String str(Long id) {
        return id != null ? id.toString() : null;
    }
}
