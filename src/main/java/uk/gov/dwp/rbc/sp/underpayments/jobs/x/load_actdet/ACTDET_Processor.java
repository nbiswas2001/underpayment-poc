package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_actdet;

import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;

public class ACTDET_Processor implements ItemProcessor<Account, Account> {


    //----------------------------------------------------
    @Override
    public Account process(Account account) throws Exception {

        return account;
    }

}
