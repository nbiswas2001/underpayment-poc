package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_circs_elig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.CircsLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.jobs.CalcStepProcessor;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_CIRCS_ELIGIBILITY;

@Slf4j
public class CalcCircsEligibility_Processor implements CalcStepProcessor, ItemProcessor<Account, Account>  {

    private CircsLogic circsLogic;
    private EligibilityLogic eligibilityLogic;

    //---------------------------
    public CalcCircsEligibility_Processor(CircsLogic circsLogic, EligibilityLogic eligibilityLogic) {
        this.circsLogic = circsLogic;
        this.eligibilityLogic = eligibilityLogic;
    }

    //---------------------------
    @Override
    public Account process(Account account) throws Exception {

        tryDo(account, () ->{
            circsLogic.createCircs(account);
            eligibilityLogic.setEligibilityBasedOnCircs(account);
        });
        return account;
    }

    //---------------------------
    @Override
    public CalcStep getStepId() {
        return CALC_CIRCS_ELIGIBILITY;
    }
}
