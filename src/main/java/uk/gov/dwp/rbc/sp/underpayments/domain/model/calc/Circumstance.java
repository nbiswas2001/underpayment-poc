package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.Rate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Getter @Setter
public class Circumstance {

    private int number;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    private StartEvent startEvent;

    private SpouseCircumstance spouseCircumstance;

    public Boolean isMarried = false;

    public String spouseAccountId;

    private List<Rate> spRates;

    private CalcResult calcResult;

    private EntitlementCalcLog entitlementCalcLog;

    private Long pkAwcm;

    public Optional<Rate> catARate() {
        return spRates.stream().filter(r -> r.getSacType().equals(SacType.CAT_A_BASIC)).findFirst();
    }

    //========================
    public enum StartEvent {
        CLAIM, // Customer claims SP
        MARRIAGE, // Customer marries
        SPOUSE_SP, // Spouse is deemed to be on SP i.e. spouse either starts claiming SP (pre decoupling) or reaches SPA (post decoupling)
        REACHED_80 // Customer is 80 years old
    }
}
