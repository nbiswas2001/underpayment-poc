package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Relationship;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.Rate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.RateValue;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.SpouseCircumstance;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Account.AgeCategory.OVER_80;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance.StartEvent.*;

@Slf4j
@Component
public class CircsLogic {

    private AccountRepo accountRepo;

    private AppConfig appConfig;

    //------------------------------------------------
    @Autowired
    public CircsLogic(AppConfig appConfig, AccountRepo accountRepo){
        this.accountRepo = accountRepo;
        this.appConfig = appConfig;
    }

    //---------------------------------------------
    public void createCircs(Account account) {

        val circumstances = new ArrayList<Circumstance>();

        //Claim circ
        val claimCircOpt = createFirstClaimCirc(account);
        if(claimCircOpt.isPresent()) circumstances.add(claimCircOpt.get());

        //Marriage circs
        circumstances.addAll(createMarriageCircs(account));

        //Spouse Claim and Spouse SPA (decoupling) circs
        circumstances.addAll(createSpouseSpCircs(account, circumstances));

        //Order circs by start date
        circumstances.sort(Comparator.comparing(Circumstance::getStartDate));

        //Set end date of each circ, if reqd.
        setCircEndDates(account, circumstances);

        //Load circs data
        populateCircumstances(account, circumstances);

        account.setCircumstances(circumstances);
    }

    //----------------------------------------------------------------
    private Optional<Circumstance> createFirstClaimCirc(Account account){

        val spStartDate = account.getSpStartDate();
        if(spStartDate == null) return Optional.empty();

        val circ = new Circumstance();
        circ.setStartEvent(CLAIM);
        circ.setStartDate(spStartDate);

        val relnOpt = account.relationshipActiveOnDate(spStartDate);
        if(relnOpt.isPresent()){
            updateCircFromRelationship(circ, relnOpt.get());
        }

        return Optional.of(circ);
    }

    //---------------------------------------------------------------
    private List<Circumstance> createMarriageCircs(Account account) {

        val result = new ArrayList<Circumstance>();

        //Not claiming yet
        val spStartDate = account.getSpStartDate();
        if(spStartDate==null) return List.of();

        for(val reln: account.getRelationships()){

            val relnStartDate = reln.getStartDate();

            //Only marriages after claim
            if(DateUtils.isDateAfter(relnStartDate, spStartDate)){
                val circ = new Circumstance();
                circ.setStartEvent(MARRIAGE);
                circ.setStartDate(relnStartDate);
                updateCircFromRelationship(circ, reln);
                result.add(circ);
            }
        }
        return result;
    }

    //------------------------------------------------------------------
    private List<Circumstance> createSpouseSpCircs(Account account,
                                                   List<Circumstance> circs) {
        val result = new ArrayList<Circumstance>();
        //Not claiming yet
        val spStartDate = account.getSpStartDate();
        if(spStartDate==null) return List.of();

        for(val circ: circs) {
            if( (circ.getStartEvent().equals(CLAIM) && circ.getIsMarried())
                    || circ.getStartEvent().equals(MARRIAGE)) {

                val spCirc = circ.getSpouseCircumstance();
                if(spCirc!= null
                    && spCirc.getAccountId() !=null //Spouse a/c exists
                    && !spCirc.getIsOnSP()){ // Spouse not already on SP (on the CLAIM or MARRIAGE)

                    // Compute the spouse's 'deemed on SP' date and event
                    OffsetDateTime deemedOnSpDate = null;
                    SpouseCircumstance.DeemedOnSpEvent deemedOnSpEvent = null;
                    if(DateUtils.isDateBefore(spCirc.getSpaDate(), dt_6_4_2010)){ //Decoupling rules start date
                        deemedOnSpDate = spCirc.getSpStartDate();
                        deemedOnSpEvent = SpouseCircumstance.DeemedOnSpEvent.CLAIM;
                    }
                    else {
                        deemedOnSpDate = spCirc.getSpaDate();
                        deemedOnSpEvent = SpouseCircumstance.DeemedOnSpEvent.SPA;
                    }

                    //if spouse's 'deemed on SP' date falls within the period of marriage
                    if(DateUtils.isDateOnOrAfter(deemedOnSpDate, circ.getStartDate())
                            && DateUtils.isDateBefore(deemedOnSpDate, spCirc.getRelationshipEndDate())
                    ){
                        Circumstance newCirc = null;
                        val newSpCirc = new SpouseCircumstance();
                        //Create new SPOUSE_SP circ
                        newCirc = new Circumstance();
                        newCirc.setStartEvent(SPOUSE_SP);
                        newCirc.setIsMarried(true);
                        newCirc.setStartDate(deemedOnSpDate);
                        newSpCirc.setDeemedOnSpEvent(deemedOnSpEvent);
                        newSpCirc.setCitizenKey(circ.getSpouseCircumstance().getCitizenKey());
                        newSpCirc.setPkPrsnToPrsn(circ.getSpouseCircumstance().getPkPrsnToPrsn());
                        newSpCirc.setPkPrsnB(circ.getSpouseCircumstance().getPkPrsnB());
                        result.add(newCirc);
                    }
                }
            }
        }
        return result;
    }

    private static final OffsetDateTime dt_6_4_2010 = OffsetDateTime.
            of(2010,4,6,0,0,0,0, ZoneOffset.UTC);


    //-----------------------------------------------------------------------------
    private void updateCircFromRelationship(Circumstance circ, Relationship reln){
        circ.setIsMarried(true);
        val newSpCirc = new SpouseCircumstance();
        newSpCirc.setCitizenKey(reln.getCitizenKey());
        newSpCirc.setPkPrsnB(reln.getPkPrsnB());
        newSpCirc.setPkPrsnToPrsn(reln.getPkPrsnToPrsn());
        newSpCirc.setRelationshipStartDate(reln.getStartDate());
        newSpCirc.setRelationshipEndDate(reln.getEndDate());
        circ.setSpouseCircumstance(newSpCirc);
    }

    //-------------------------------------------------------------------------------
    //TODO Change this way of finding Cat D. Rather check every year after 80th b'day for drop in entitlement
    private Optional<Circumstance> createReached80Circ(Account ac) {
        if(ac.getAgeCategory().equals(OVER_80)){
            val circ = new Circumstance();
            val birthDay80 = ac.getDateOfBirth().plusYears(80);

            //Add circ only if it starts before the calc date
            if(birthDay80.isAfter(appConfig.getCalcDate())){
                return Optional.empty();
            }
            circ.setStartDate(birthDay80);
            circ.setStartEvent(REACHED_80);
            return Optional.of(circ);
        }
        return Optional.empty();
    }


    //-----------------------------------------------------------------------
    private void setCircEndDates(Account account, List<Circumstance> circumstances){

        var i = 0;
        val len = circumstances.size();
        for(val circ : circumstances){
            val relnEndDate = circ.getIsMarried() ? circ.getSpouseCircumstance().getRelationshipEndDate() : null;
            val dod = account.getDateOfDeath();
            val calcDate = appConfig.getCalcDate();


            //If this is the last circ and if the customer is dead, the circ end date is dateOfDeath
            if(i == len-1){
                circ.setEndDate(DateUtils.earliestOf(relnEndDate, dod, calcDate));
             }
            else { //i < len -1, i.e. not last circ
                //end date is the day before the next circ's start date
                val circEndDate = circumstances.get(i+1).getStartDate().minusDays(1);
                circ.setEndDate(DateUtils.earliestOf(relnEndDate, circEndDate));
            }

            //Sequence no.
            circ.setNumber(i);
            i++;
        }
    }

    //----------------------------------------------------------------------------------------
    private void populateCircumstances(Account account, List<Circumstance> circumstances) {
        for (val circ : circumstances) {
            val awardOpt = account.awardActiveOnDate(circ.getStartDate());
            if (awardOpt.isPresent()) {
                val award = awardOpt.get();
                circ.setPkAwcm(award.getPkAwcm());

                //Add rates from award
                circ.setSpRates(award.spRates());
            }

            if (circ.isMarried) {

                val ninoB = circ.getSpouseCircumstance().getPkPrsnB();
                val spouseAcOpt = accountRepo.findByPkPrsn(ninoB);

                //Load spouse circ
                if (spouseAcOpt.isPresent()) {
                    val spouseAc = spouseAcOpt.get();
                    val spouseCirc = circ.getSpouseCircumstance();
                    spouseCirc.setSex(spouseAc.getSex());
                    spouseCirc.setSpaDate(spouseAc.getSpaDate());
                    spouseCirc.setAccountId(spouseAc.getId());

                    val spAwardOpt = spouseAc.awardActiveOnDate(circ.getStartDate());

                    if (spAwardOpt.isPresent()) {
                        val spAward = spAwardOpt.get();
                        spouseCirc.setPkAwcm(spAward.getPkAwcm());
                        spouseCirc.setIsOnSP(true);
                        spouseCirc.setSpStartDate(spouseAc.getSpStartDate()); //TODO Check if its benefitStartDate or entitlementStartDate
                        val catASacOpt = spAward.findSacByType(SacType.CAT_A_BASIC);
                        if (catASacOpt.isPresent()) {
                            spouseCirc.setCatARate(catASacOpt.get().getRate());
                        }
                    } else {
                        spouseCirc.setIsOnSP(false);
                    }
                } else {
                    throw new UeException("Could not find account for spouse NINO " + ninoB);
                }
            }
        }

    }

}
