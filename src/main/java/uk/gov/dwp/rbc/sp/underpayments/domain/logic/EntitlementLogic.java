package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.Rate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.EntitlementCalcLogEntry;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.EntitlementCalcLog;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Code.ELIGIBLE;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Code.ENTITLED;

@Slf4j
@Component
public class EntitlementLogic {

    private AppConfig config;

    private F1010Rates rates;

    @Autowired
    public EntitlementLogic(AppConfig config, F1010Rates rates) {
        this.config = config;
        this.rates = rates;
    }

    //---------------------------------------------
    public void calculateEntitlement(Account account) {

        var totalUnderpaid = 0;
        for(val circ : account.getCircumstances()){
            val calcResult = circ.getCalcResult();
            if(calcResult == null){
                throw new UeException("CalcResult is null");
            }
            val isEligible = calcResult.getCode().equals(ELIGIBLE);

            if(isEligible){

                //calc entitlement for circ
                val calcLog = calculateCircEntitlement(circ, account);
                //set the result on the circ
                circ.setEntitlementCalcLog(calcLog);

                if(calcLog.getTotalAmount() > 0){
                    circ.getCalcResult().setUnderpaidAmount(calcLog.getTotalAmount());
                    circ.getCalcResult().setCode(ENTITLED);
                }

                //total up the entitlement amount
                totalUnderpaid += calcLog.getTotalAmount();
            }
        }

        account.getCalcResult().setUnderpaidAmount(totalUnderpaid);

        if (totalUnderpaid > 0) {
            account.getCalcResult().setCode(ENTITLED);
        }
    }

    //--------------------------------------------------------------------------------------
    EntitlementCalcLog calculateCircEntitlement(Circumstance circ,
                                                Account account){

        EntitlementCalcLog result = new EntitlementCalcLog();

        //Set up for totals
        var totalAmountForCirc = 0;
        var totalWeeksInCirc = 0;

        var partWeekPmt = false;
        //Part payment can only happen for the first circ (CLAIM) and is indicated by the isPartWeek flag
        if(circ.getNumber() == 0 && account.getIsParkWeekPayment()){
            partWeekPmt = true;
        }

        //The day of the week on which the person is paid
        var payDay = account.getPayDay();

        //TODO how to set this?
        // assuming If never had an award then set payday to 1 (Mon)
        if(payDay == null || payDay == 0) payDay = 1;

        //If the payment is made on the first day of the week (true) or the last (false)
        val isPmtInAdvance = account.getIsPaymentInAdvance();

        //entitlment start date
        val entitlementStartDate = circ.getStartDate();

        //calculate date of first payment
        val startDateWeekDay = entitlementStartDate.getDayOfWeek().getValue();
        val diff = payDay - startDateWeekDay + (payDay >= startDateWeekDay? 0 : 7);
        val offset = isPmtInAdvance ? 0 : 1;
        val startDateOfFirstPmtWeek = entitlementStartDate.plusDays(diff).plusDays(offset);

        //entitlement end date is either explicit end date or today (i.e. date of calc)
        val entitlementEndDate = circ.getEndDate() == null? config.getCalcDate(): circ.getEndDate();

        //**** Calculate rate Cat BL / Cat D entitled to ****

        //Get current Cat A, if any
        Optional<Rate> catARateOpt =  circ.catARate();
        Rate awardRate;

        //This gives the indication is it is a Cat BL or Cat D award
        val reason = circ.getCalcResult().getReason();
        awardRate = new Rate();

        //Get the applicable rate
        if(reason.equals(CalcResult.Reason.CAT_BL)){
            awardRate.setSacType(CAT_BL_BASIC);
            awardRate.setValueFrom(circ.getSpouseCircumstance().getCatARate());
        }
        else if(reason.equals(CalcResult.Reason.CAT_D)){
            awardRate.setSacType(CAT_D_BASIC);
            awardRate.setPercentRate(100_00);
        }
        else throw new UeException("Unexpected Eligibility.Reason "+reason);

        //get the first rate amount - based on date of first payment
        //  getWeeklyAward() is the main function for calculating weekly award
        var weeklyAward = getWeeklyAward(awardRate, catARateOpt, entitlementStartDate);

        //Set values in the result
        result.setSacType(awardRate.getSacType());
        if(weeklyAward.compositePctRate != -1) {
            result.setComposite(true); // i.e. the customer already has A Cat A, so this (BL/D) is a composite award
            result.setCompositePctRate(weeklyAward.compositePctRate);
        }

        //add the calc log entry for the part week payment if required
        if(partWeekPmt){
            val partWeekLogEntry = new EntitlementCalcLogEntry();

            //Find the number of days in the first part week i.e. between entitlement start date and first payment date
            partWeekLogEntry.setStartDate(entitlementStartDate);
            partWeekLogEntry.setEndDate(startDateOfFirstPmtWeek.minusDays(1));
            val partWeekDays = ChronoUnit.DAYS.between(partWeekLogEntry.getStartDate(), partWeekLogEntry.getEndDate());

            partWeekLogEntry.setRateAmount(weeklyAward.amount);

            //The weekly amount is pro-rated
            val amt = Math.toIntExact(Math.round(((float)weeklyAward.amount) / 7 * partWeekDays));
            partWeekLogEntry.setTotalAmount(amt);

            //Set values in result
            partWeekLogEntry.setNumWeeks(0); //i.e. part week
            result.setPartWeek(true);
            result.setPartWeekDays(Math.toIntExact(partWeekDays));
            result.getEntries().add(partWeekLogEntry);
        }

        //loop
        while(true){

            //start date of this calc entry period
            var entryStartDate = startDateOfFirstPmtWeek.isBefore(weeklyAward.startDate)?
                    weeklyAward.startDate
                    : startDateOfFirstPmtWeek;

            //evaluate if this will be the last entitlement calc entry for this circ
            val isLastEntry = DateUtils.isDateOnOrBefore(entitlementEndDate, weeklyAward.endDate);

            //end date of calc entry period
            var entryEndDate = isLastEntry ?  entitlementEndDate: weeklyAward.endDate;

            // days in this period (use float for division in next step)
            val totalDays = ChronoUnit.DAYS.between(entryStartDate, entryEndDate);

            //weeks in this period
            val wholeWeeks = Math.toIntExact((long) Math.ceil(((float)totalDays) / 7));

            //Total amt for this entry
            val totalAwardForEntry = wholeWeeks * weeklyAward.amount;

            //Populate a calc result entry and add it to result
            val logEntry = new EntitlementCalcLogEntry();
            logEntry.setStartDate(entryStartDate);
            logEntry.setEndDate(entryEndDate.minusDays(1));
            logEntry.setNumWeeks(wholeWeeks);
            logEntry.setRateAmount(weeklyAward.amount);
            logEntry.setTotalAmount(totalAwardForEntry);
            result.getEntries().add(logEntry);

            //Keep totals
            totalAmountForCirc += totalAwardForEntry;
            totalWeeksInCirc += wholeWeeks;

            //if this was the last entry then end
            if(isLastEntry) break;
            // else get the next rate entry (pick a date a week later)
            else weeklyAward = getWeeklyAward(awardRate, catARateOpt, weeklyAward.endDate.plusWeeks(1));
        }

        // If Cat D, add additional amount @ 25p / week
        val catDAdd = awardRate.getSacType().equals(SacType.CAT_D_BASIC)? totalWeeksInCirc * 25 : 0;
        result.setCatDAddedAmount(catDAdd);

        //Put in the totals
        result.setTotalWeeks(totalWeeksInCirc);
        result.setTotalAmount(totalAmountForCirc + catDAdd);
        return result;

    }

    //-----------------------------------------------------------------------
    // Gets the weekly award amount based on customer's SpRate and the F1010
    //   rate amount for given date
    private WeeklyAward getWeeklyAward(Rate awardRate,
                                       Optional<Rate> catARateOpt,
                                       OffsetDateTime forDate){

        val sacType = awardRate.getSacType();

        //Get the applicable F1010 entry for the given date
        val awardRateEntry = rates.getEntry(sacType, forDate);

        //Calculate the weekly amount
        val weeklyAwardAmt = awardRate.amountFor(awardRateEntry);

        val result = new WeeklyAward();

        //If the customer already has a Cat A, then the award amount has to be capped
        if(catARateOpt.isPresent()){
            //Get the max amount payable for this SAC
            val maxAwardAmt = awardRateEntry.max;

            //Get the Cat A amount
            val catARateEntry = rates.getEntry(CAT_A_BASIC, forDate);
            val catAAmt = catARateOpt.get().amountFor(catARateEntry);

            //Cap the combined payout to the max possible for the SAC)
            val uncappedAmt = weeklyAwardAmt + catAAmt;
            val compositeAmt = uncappedAmt > maxAwardAmt ? maxAwardAmt : uncappedAmt;

            //Note the weekly award amount
            result.amount = compositeAmt;

            //Note the effective composite rate in the result for reference. Will be needed
            // for putting into PSCS
            result.compositePctRate = ((double)compositeAmt)/ maxAwardAmt * 100;
        }
        else {
            //Note the weekly award amount
            result.amount = weeklyAwardAmt;
        }

        //Note the start and end date of the F1010 entry period
        result.startDate = awardRateEntry.startDate;
        result.endDate = awardRateEntry.endDate;

        return result;
    }

    //---------------------------------------------------------
    private Rate getAwardRate(CalcResult.Reason reason, Rate spouseCatA){
        Rate awardRate = new Rate();
        awardRate = new Rate();
        if(reason.equals(CalcResult.Reason.CAT_BL)){
            awardRate.setSacType(CAT_BL_BASIC);
            awardRate.setValueFrom(spouseCatA);


        }
        else if(reason.equals(CalcResult.Reason.CAT_D)){
            awardRate.setSacType(CAT_D_BASIC);
            awardRate.setPercentRate(100_00);
        }
        else throw new UeException("Unexpected Eligibility.Reason "+reason);

        return awardRate;
    }

    //=========================
    private class WeeklyAward {
        public int amount;
        public OffsetDateTime startDate;
        public OffsetDateTime endDate;
        public double compositePctRate = -1;
    }
}
