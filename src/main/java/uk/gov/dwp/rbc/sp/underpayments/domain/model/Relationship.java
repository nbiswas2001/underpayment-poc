package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;
import uk.gov.dwp.rbc.sp.underpayments.jobs.UeEntity;

import java.time.OffsetDateTime;

@Slf4j
@Getter @Setter
public class Relationship implements UeEntity {

    private String citizenKey;

    private Type type = Type.SPOUSE;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    private Boolean startVerified;

    private Boolean endVerified;

    private EndReason endReason;

    private Problems problems = new Problems();

    private long pkPrsnToPrsn;

    private String pkPrsnB;

    @Override
    public Logger logger() {return log;}


    //===================
    public enum Type {
        SPOUSE,
        CHILD,
        OTHER
    }

    //===================
    public enum EndReason {
        NA,
        DEATH,
        DIVORCE,
        MARRIAGE_VOID,
        SEPARATED,
        OTHER
    }

}
