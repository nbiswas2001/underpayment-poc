package uk.gov.dwp.rbc.sp.underpayments.services.mi;

public class FlagsRec {

    public FlagsRec(String citizenKey, Long reportingFlags, Long errorFlags) {
        this.citizenKey = citizenKey;
        this.reportingFlags = reportingFlags;
        this.errorFlags = errorFlags;
    }

    public String citizenKey;
    public Long reportingFlags;
    public Long errorFlags;

    public static final String C = "uk.gov.dwp.rbc.sp.underpayments.services.mi.FlagsRec";
}
