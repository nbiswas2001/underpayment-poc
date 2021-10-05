package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.Logger;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.*;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;
import uk.gov.dwp.rbc.sp.underpayments.jobs.UeEntity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter @Setter
public class SpAward implements UeEntity {

    private OffsetDateTime startDate;

    private Status status;

    private int awcmNum;

    private long pkPrlbn;
    private long pkAw;
    private long pkAwcm;
    private long pkClm;

    private Problems problems = new Problems();

    public List<SubAwardComponent> subAwardComponents;

    //------------------------------------------------------------
    public Optional<SpSubAwardComponent> findSacByType(SacType type) {

        val sacs = getSubAwardComponents();
        return sacs.stream()
                .filter(sac -> sac instanceof SpSubAwardComponent)
                .map( sac -> (SpSubAwardComponent) sac)
                .filter(spSac -> spSac.getRate().getSacType().equals(type))
                .findFirst();
    }

    //----------------------------------------------
    public List<Rate> spRates() {
        List<Rate> spRates = new ArrayList<>();
        for (val sacType : RateValue.maxAwardThresholds.keySet()) {
            val sacOpt = findSacByType(sacType);
            if (sacOpt.isPresent()) {
                val rate = sacOpt.get().getRate();
                spRates.add(rate);
            }
        }
        return spRates;
    }

    //----------------------------
    @Override
    public Logger logger() {return log;}


    //====================
    public enum Status {
        PAYABLE
    }
}
