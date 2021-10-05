package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;
import uk.gov.dwp.rbc.sp.underpayments.jobs.UeEntity;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Document
@Getter @Setter
public class Account implements UeEntity {

    @Id
    private String id;

    private String citizenKey;

    //---------------------------------------------------------
    // Citizen represents PII and it is held as encypted JSON
    private String citizenData;

    private OffsetDateTime dateOfBirth;

    private OffsetDateTime dateOfDeath;

    private Boolean isDobVerified;

    private Boolean isDodVerified;

    private Boolean isInternational = false;

    private Integer numRelationships = 0;

    private Boolean isOnPC = false;

    private OffsetDateTime spStartDate;

    // 1-5, Mon-Fri
    private Integer payDay;

    //If SP is paid in advance or in arrears
    private Boolean isPaymentInAdvance = false;

    //If part week payment is allowed
    private Boolean isParkWeekPayment = false;

    private OffsetDateTime paymentsMadeUpto;

    private Sex sex;

    private OffsetDateTime spaDate;

    private AgeCategory ageCategory;

    //TODO - Do we need this? If not, remove
    //private String notepad;

    private List<Relationship> relationships = new ArrayList<>();

    private List<SpAward> awards = new ArrayList<>();

    private List<Circumstance> circumstances = new ArrayList<>();

    private CalcResult calcResult;

    private CalcStep stepCompleted = CalcStep.NEW;

    private Problems problems = new Problems();

    private boolean isEncrypted;

    private long pkPrlbn;
    private String pkPrsn;
    private String xiAreaId;

    private int partition;
    private String schema;

    @Override
    public Logger logger() {return log;}

    //--------------------------------------------
    public Optional<SpAward> award(Long pkAwcm) {
        if(pkAwcm == null) return Optional.empty();
        else return getAwards().stream().filter(aw -> aw.getPkAwcm() == pkAwcm).findFirst();
    }

    //-------------------------------------------------------------------------------------------------
    public Optional<Relationship> relationshipActiveOnDate(OffsetDateTime onDate){

        for (val rel : getRelationships()) {
            if (DateUtils.isDateBetween(onDate, rel.getStartDate(), rel.getEndDate())) {
                return Optional.of(rel);
            }
        }
        return Optional.empty();
    }

    //-------------------------------------------------------------------------------------------------
    public Optional<SpAward> awardActiveOnDate(OffsetDateTime onDate){

        if(onDate == null) return Optional.empty();

        SpAward latestAward = null;
        for (val aw : getAwards()) {
            if(DateUtils.isDateOnOrBefore(aw.getStartDate(), onDate.plusDays(7))){
                latestAward = aw;
            }
        }
        return Optional.ofNullable(latestAward);
    }

    //===============================
    public enum BenefitAcStatus {
        CLOSED,
        LIVE,
        DORMANT,
        DISALLOWED,
        DEAD,
        MANCOB_OR,
        ASST_REQD,
        DEATH_NOTIFIED,
        REINSTATED,
        OVERPMT_RECOVERY,
        APPEAL_RECVD,
        OTHER
    }

    public enum AgeCategory {
        UNDER_SPA,
        SPA,
        OVER_80
    }


}
