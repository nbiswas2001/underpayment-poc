package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@ToString
public class F1010RateEntry {
    public OffsetDateTime startDate;
    public OffsetDateTime endDate;
    public int max;
    F1010RateEntry prevEntry;
}
