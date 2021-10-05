package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Sex;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.SpAward;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.Rate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.RateValue;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance;
import uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig.AccountPartitionGenerator;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Account.AgeCategory.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Code.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Reason.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance.StartEvent.REACHED_80;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance.StartEvent.SPOUSE_SP;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.WarningFlags.Warning.*;

/**
 * Pseudocode - https://dwpdigital.atlassian.net/wiki/spaces/UE/pages/132104650862/Algorithm
 */

@Component
public class EligibilityLogic {

    @Autowired
    private SpaLogic spaLogic;

    @Autowired
    private AppConfig config;

    @Autowired
    F1010Rates f1010Rates;

    @Autowired
    AccountPartitionGenerator partitioner;

    //---------------------------------------------------------------
    public void setEligibilityBasedOnAccount(Account account) {

        val ageCategory = spaLogic.getAgeCategory(account, config.getCalcDate());

        account.setAgeCategory(ageCategory);
        if(account.getDateOfDeath() != null && !account.getIsDodVerified()){
            account.getProblems().warnings().set(PRSN_UNVERIFIED_DOD);
        }

        val spaDate = account.getSpaDate();
        val sex = account.getSex();

        if(spaDate.isBefore(dt_15_11_1976)){
            account.getProblems().warnings().set(AC_PRE_PSCS_SPA_DATE);
        }
        if(account.getIsInternational()){
            account.getProblems().warnings().set(AC_INTERNATIONAL);
        }

        //Default position at this stage
        val result = account.getCalcResult();
        result.setCode(MAYBE_ELIGIBLE);

        if(spaDate.isAfter(dt_5_4_2016)) {
            result.setCode(INELIGIBLE);
            result.setReason(ON_NEW_SP);
        }
        else if (ageCategory.equals(UNDER_SPA)) {
            result.setCode(INELIGIBLE);
            result.setReason(NOT_REACHED_SPA);
        }
        else {
            //SPA
            if (ageCategory.equals(SPA)) {

                //Never married
                if (account.getNumRelationships() == 0) {
                    result.setCode(INELIGIBLE);
                    result.setReason(NEVER_MARRIED);
                }
                //Male with SPA date before 6/4/2010
                else if (sex.equals(Sex.M) && spaDate.isBefore(dt_6_4_2010)) {
                    result.setCode(INELIGIBLE);
                    result.setReason(MALE_PRE_2010_SPA_DATE);
                }
            }
            // OVER_80 remains on MAYBE_ELIGIBLE
        }


        account.setPartition(partitioner.getPartition());
        account.setSchema(account.getXiAreaId().substring(0,4));
        result.setAcEligCalculated(true);
    }


    //-------------------------------------------------------------------
    public void setEligibilityBasedOnCircs(Account account) {

        val sex = account.getSex();
        val spaDate = account.getSpaDate();
        var eligibleBL = false;
        var eligibleD = false;
        var needsToClaim = false;
        var spouseOnNewSP = false;

        for(val circ : account.getCircumstances()){

            //first complete the circs other than REACHED_80
            if(circ.getStartEvent().equals(REACHED_80)) continue;

            //Do basic BL checks again
            val circResult = new CalcResult();

            if (account.getNumRelationships() == 0) {
                circResult.setCode(INELIGIBLE);
                circResult.setReason(NEVER_MARRIED);
            }
            else if (sex.equals(Sex.M) && spaDate.isBefore(dt_6_4_2010)) {
                circResult.setCode(INELIGIBLE);
                circResult.setReason(MALE_PRE_2010_SPA_DATE);
            }
            else {
                if (isOnMaxAward(circ)) { //On max A, AB, BL
                    circResult.setCode(INELIGIBLE);
                    circResult.setReason(ON_MAX_AWARD);
                } else {
                    if (circ.getIsMarried()) {

                        val spouseCirc = circ.getSpouseCircumstance();
                        val spouseSex = spouseCirc.getSex();
                        val spouseSpaDate = spouseCirc.getSpaDate();
                        val spouseCatA = spouseCirc.getCatARate();

                        if (citizenQualifiesBasedOnSpouseSex(sex, spaDate, spouseSex, spouseSpaDate)) {
                            if(circ.getSpouseCircumstance().getSpaDate().isAfter(dt_5_4_2016)){
                                spouseOnNewSP = true;
                                break;
                            }
                            if (spouseCirc.getCatARate() == null) {
                                circResult.setCode(INELIGIBLE);
                                circResult.setReason(NO_SPOUSE_CAT_A);
                            } else if (spouseCirc.getCatARate().isLessThan(RateValue.minCatAContribution)) {
                                circResult.setCode(INELIGIBLE);
                                circResult.setReason(SPOUSE_CAT_A_BELOW_MIN);
                            } else if (isOnMaxBL(circ, spouseCatA)) { //On max BL award
                                circResult.setCode(INELIGIBLE);
                                circResult.setReason(ON_MAX_AWARD);
                            } else {
                                //Set Cat BL eligibility
                                circResult.setCode(ELIGIBLE);
                                circResult.setReason(CAT_BL);
                                eligibleBL = true;

                                //Pre 17/3/2008 customer had to explicitly claim Cat BL when their spouse went on SP
                                if(account.getSpaDate().isBefore(dt_17_3_2008)
                                    && circ.getStartEvent().equals(SPOUSE_SP)
                                    && circ.getStartDate().isBefore(dt_17_3_2008)){
                                    needsToClaim = true;
                                }
                                circResult.setNeedsToClaim(needsToClaim);
                            }
                        } else {
                            circResult.setCode(INELIGIBLE);
                            circResult.setReason(WRONG_SPOUSE_SEX);
                        }
                    } else {
                        circResult.setCode(INELIGIBLE);
                        circResult.setReason(NOT_MARRIED);
                    }
                }
            }
            circ.setCalcResult(circResult);
        }

        //Cat D
        if(account.getAgeCategory().equals(OVER_80)){
            val cr = account.getCalcResult();
            val awCount = account.getAwards().size();
            if(awCount > 0) {
                val lastAward = account.getAwards().get(awCount - 1);
                if (lastAward.findSacByType(SacType.CAT_D_BASIC).isPresent()) {
                    cr.setCode(INELIGIBLE);
                    cr.setReason(HAS_CAT_D);
                }
                else {
                    val birthDay80 = account.getDateOfBirth().plusYears(80);
                    val award80Opt = account.awardActiveOnDate(birthDay80);
                    if (!isOnMaxD(award80Opt)) {
                        eligibleD = true;
                    }
                    else {
                        cr.setCode(INELIGIBLE);
                        cr.setReason(ON_MAX_AWARD);
                    }
                }
            }
            else {
                eligibleD = true;
            }
        }

        //Set calcResult values
        val acResult = account.getCalcResult();
        if(spouseOnNewSP){
            acResult.setCode(TOO_COMPLEX);
            acResult.setReason(SPOUSE_ON_NEW_SP);
        }
        else if(eligibleBL){
            acResult.setCode(ELIGIBLE);
            acResult.setReason(CAT_BL);
            acResult.setNeedsToClaim(needsToClaim);
        }

        if(eligibleD){  //Eligible for D (may or may not be eligible for BL)
            acResult.setCode(ELIGIBLE);
            acResult.setReason(eligibleBL? CAT_BL_AND_D : CAT_D);
        }
        else if(!eligibleBL) { //Not eligible for BL or D
            acResult.setCode(INELIGIBLE);
            acResult.setReason(NO_ELIGIBLE_CIRCS);
        }
    }

    //-----------------------------------------------------------------------------
    private boolean citizenQualifiesBasedOnSpouseSex(Sex sex, OffsetDateTime spaDate,
                                                     Sex spouseSex, OffsetDateTime spouseSpaDate){

        //If customer is M with spaDate < 6/4/2010 then false - this is already filtered out in 'Calc elig based on account'
        if(sex.equals(Sex.F) && spouseSex.equals(Sex.M)) return true;
        else {
            if(DateUtils.isDateOnOrAfter(spaDate, dt_6_4_2010)){
                if(spouseSex.equals(Sex.F) && DateUtils.isDateOnOrAfter(spouseSpaDate, dt_6_5_2010 )) return true;
                else if(spouseSex.equals(Sex.M) && DateUtils.isDateOnOrAfter(spouseSpaDate, dt_5_4_2015 )) return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------
    private boolean isOnMaxAward(Circumstance circ){
        for(val rate : circ.getSpRates()){
            if (rate.isAtLeast(RateValue.maxAwardThresholds.get(rate.getSacType()))) return true;
        }
        return false;
    }

    //-------------------------------------------------------
    // Note this does not check composite awards
    private boolean isOnMaxBL(Circumstance circ, Rate spouseCatA){

        for(val rate : circ.getSpRates()){
            if(rate.getSacType().equals(SacType.CAT_BL_BASIC)
            || rate.getSacType().equals(SacType.COMPOSITE_ABL_BC)){
                if(rate.isAtLeast(spouseCatA)) return true;
            }
        }
        return false;
    }
    //-------------------------------------------------------
    private static Set<SacType> catDRelevantSacs = Set.of(CAT_A_BASIC, CAT_B_BASIC, CAT_BL_BASIC, IAA_HIGH, IAA_LOW);
    private static Set<SacType> iaaSacs = Set.of(IAA_HIGH, IAA_LOW);

    private boolean isOnMaxD(Optional<SpAward> awardOpt){
        if(awardOpt.isPresent()) {
            val aw = awardOpt.get();

            //Do basic check based on max award thresholds
            for (val rate : aw.spRates()) {
                if (rate.isAtLeast(RateValue.maxAwardThresholds.get(rate.getSacType()))) return true;
            }

            //if still false, then do detailed check based on amounts from various sacs and incs
            int maxCatD = f1010Rates.getEntry(CAT_D_BASIC, aw.getStartDate()).max;
            float totalAward = 0;
            for (val rate : aw.spRates()) {
                val sacType = rate.getSacType();
                if(catDRelevantSacs.contains(sacType)) {
                    val rateEntry = f1010Rates.getEntry(sacType, aw.getStartDate());
                    if(iaaSacs.contains(sacType)){
                        totalAward += rateEntry.max;
                    }
                    else {
                        totalAward += rate.amountFor(rateEntry);
                        val bcIncRateEntry = f1010Rates.getEntry(BC_INCS, aw.getStartDate());
                        totalAward += rate.amountFor(bcIncRateEntry);
                    }
                }
            }
            if(totalAward >= maxCatD) return true;
        }
        return false;
    }
    //-----------------------------------------------------------------------------------------
    private static final OffsetDateTime dt_6_4_2010 = dt(6,4,2010);
    private static final OffsetDateTime dt_6_5_2010 = dt(6,5,2010);
    private static final OffsetDateTime dt_5_4_2015 = dt(5,4,2015);
    private static final OffsetDateTime dt_17_3_2008 = dt(17,3,2008);
    private static final OffsetDateTime dt_5_4_2016 = dt(5,4,2016);
    private static final OffsetDateTime dt_15_11_1976 = dt(15,11,1976);

    private static OffsetDateTime dt(int d, int m, int y){
        return DateUtils.dt(d,m,y);
    }


}
