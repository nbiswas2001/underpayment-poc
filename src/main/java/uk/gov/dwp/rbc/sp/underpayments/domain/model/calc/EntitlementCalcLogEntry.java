package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@ToString
@Getter @Setter
public class EntitlementCalcLogEntry {

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private int rateAmount;
    private int numWeeks;
    private int totalAmount;
}
