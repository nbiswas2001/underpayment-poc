package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.val;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Code.*;

public interface CalcStepProcessor {

    CalcStep getStepId();

    //---------------------------------------
    default void tryDo(Account account, Runnable block){
        val prob = account.getProblems();
        val step = getStepId();

        if(!prob.getHasErrors()) {
            try {
                block.run();
            } catch (Exception e){
                switch (step){
                    case CALC_AC_ELIGIBILITY: prob.errors().set(ErrorFlags.Error.EXCEPTION_CALC_AC_ELIGIBILITY); break;
                    case CALC_CIRCS_ELIGIBILITY: prob.errors().set(ErrorFlags.Error.EXCEPTION_CALC_CIRCS_ELIGIBILITY); break;
                    case CALC_ENTITLEMENT: prob.errors().set(ErrorFlags.Error.EXCEPTION_CALC_ENTITLEMENT); break;
                }
                prob.setHasErrors(true);
                UeEntity.logException(e, prob, account.logger(), account.getCitizenKey());
            }

            val cr = account.getCalcResult();
            switch (step){
                case CALC_AC_ELIGIBILITY:
                    if(cr.getCode().equals(MAYBE_ELIGIBLE)) cr.setMaybeEligible(true);
                    break;

                case CALC_CIRCS_ELIGIBILITY:
                    if(cr.getCode().equals(ELIGIBLE)) cr.setEligible(true);
                    cr.setCircsEligCalculated(true);
                    break;

                case CALC_ENTITLEMENT:
                    if(cr.getCode().equals(ENTITLED)) cr.setEntitled(true);
                    break;
            }
        }
        account.setStepCompleted(step);
    }
}
