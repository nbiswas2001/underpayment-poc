package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_pytac;

import lombok.val;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.BankAccount;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Citizen;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.JsonUtils;

public class PYTAC_Processor implements ItemProcessor<R2577PYTAC, Account> {

    private AccountRepo accountRepo;

    private CryptoUtils cryptoUtils;

    //----------------------------------------------------
    public PYTAC_Processor(AccountRepo accountRepo,
                           CryptoUtils cryptoUtils) {
        this.accountRepo = accountRepo;
        this.cryptoUtils = cryptoUtils;
    }

    //----------------------------------------------------
    @Override
    public Account process(R2577PYTAC pytac) throws Exception {

        val accountOpt = accountRepo.findByPkPrsn(pytac.PK_R2577_NINO);

        if(accountOpt.isPresent()) {
            val account = accountOpt.get();
            account.tryLoad(acErr -> {
                val bankAc = new BankAccount();
                val cdStr = account.getCitizenData();
                val citizenData = account.isEncrypted() ? cryptoUtils.decrypt(cdStr) : cdStr;
                val citizen = JsonUtils.fromJson(citizenData, Citizen.class);
                citizen.setBankAccount(bankAc);

                val newCdStr = JsonUtils.toJson(citizen);
                val newCitizenData = account.isEncrypted() ? cryptoUtils.encrypt(newCdStr) : newCdStr;
                account.setCitizenData(newCitizenData);

            });
            return account;
        }
        else return null;
    }

}
