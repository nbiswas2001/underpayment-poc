package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CalcResult {

    private Code code = Code.NEW;

    private Reason reason = Reason.NONE;

    private Boolean needsToClaim  = false;

    private Integer underpaidAmount = 0;

    private Long reportingFlagsData = 0L;

    //Step completed
    private Boolean relnsLoaded = false;
    private Boolean awardsLoaded = false;
    private Boolean acEligCalculated = false;
    private Boolean maybeEligible = false;
    private Boolean circsEligCalculated = false;
    private Boolean eligible = false;
    private Boolean entitled = false;

    //===================
    public enum Code {
        NEW,
        DATA_ERROR,
        MAYBE_ELIGIBLE,
        ELIGIBLE,
        INELIGIBLE,
        ENTITLED,
        TOO_COMPLEX
    }

    public enum Reason {
        //For NOT_CALCULATED
        NONE,

        //For NOT_ELIGIBLE at Eligibility Filter 1
        ON_NEW_SP,
        NOT_REACHED_SPA,
        MALE_PRE_2010_SPA_DATE,
        NEVER_MARRIED,
        ON_MAX_AWARD,

        //For NOT_ELIGIBLE at Eligibility Filter 2
        NO_ELIGIBLE_CIRCS,
        HAS_CAT_D,

        //For Circ
        NOT_MARRIED,
        WRONG_SPOUSE_SEX,
        NO_SPOUSE_CAT_A,
        SPOUSE_CAT_A_BELOW_MIN,

        //For TOO_COMPLEX
        PRE_PSCS_SPA_DATE,
        INTERNATIONAL,
        SPOUSE_ON_NEW_SP,

        //For Eligible
        CAT_D,
        CAT_BL,
        CAT_BL_AND_D
    }



    public static final String Code_ = "uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult$Code.";
}
