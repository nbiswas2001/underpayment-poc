package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class EntitlementCalcLog {

    private SacType sacType;
    private boolean isPartWeek;
    private int partWeekDays;
    private int totalWeeks;
    private int catDAddedAmount;
    private int totalAmount;
    private boolean isComposite;
    private double compositePctRate;
    private List<EntitlementCalcLogEntry> entries = new ArrayList<>();
}
