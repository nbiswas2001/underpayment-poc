package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EntitlementLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.jobs.CalcStepProcessor;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_ENTITLEMENT;

@Slf4j
public class CalcEntitlement_Processor implements CalcStepProcessor, ItemProcessor<Account, Account>  {

    private EntitlementLogic entitlementLogic;

    public CalcEntitlement_Processor(EntitlementLogic entitlementLogic) {
        this.entitlementLogic = entitlementLogic;
    }

    //------------------------------
    @Override
    public Account process(Account account) throws Exception {

        tryDo(account, () ->{
            entitlementLogic.calculateEntitlement(account);
        });

        return account;
    }

    //------------------------------
    @Override
    public CalcStep getStepId() {
        return CALC_ENTITLEMENT;
    }
}
