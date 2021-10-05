package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.jobs.CalcStepProcessor;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_AC_ELIGIBILITY;


@Slf4j
public class CalcAccountEligibility_Processor implements CalcStepProcessor, ItemProcessor<Account, Account>  {

    private EligibilityLogic eligibilityLogic;

    public CalcAccountEligibility_Processor(EligibilityLogic eligibilityLogic) {
        this.eligibilityLogic = eligibilityLogic;
    }

    //---------------------------
    @Override
    public Account process(Account account) throws Exception {

        tryDo(account, () ->  {
            eligibilityLogic.setEligibilityBasedOnAccount(account);
        });
        return account;
    }

    //---------------------------
    @Override
    public CalcStep getStepId() {
        return CALC_AC_ELIGIBILITY;
    }
}
