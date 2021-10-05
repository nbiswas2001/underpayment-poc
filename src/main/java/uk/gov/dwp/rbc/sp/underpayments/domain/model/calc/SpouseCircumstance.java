package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Sex;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.Rate;

import java.time.OffsetDateTime;

@Getter @Setter
public class SpouseCircumstance {

    private String citizenKey;

    private Sex sex;

    private OffsetDateTime spaDate;

    private OffsetDateTime spStartDate;

    private Boolean isOnSP;

    private DeemedOnSpEvent deemedOnSpEvent;

    private OffsetDateTime relationshipStartDate;

    private OffsetDateTime relationshipEndDate;

    private String accountId;

    private Long pkPrsnToPrsn;

    private String pkPrsnB;

    private Long pkAwcm;

    private Rate catARate;

    //================================
    public enum DeemedOnSpEvent {
        CLAIM,
        SPA
    }
}
