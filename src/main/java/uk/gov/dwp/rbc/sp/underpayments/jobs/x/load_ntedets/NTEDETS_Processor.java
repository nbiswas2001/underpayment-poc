package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_ntedets;

import lombok.val;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

public class NTEDETS_Processor implements ItemProcessor<R2816NTEDETS, Account> {

    private AccountRepo accountRepo;

    private CryptoUtils cryptoUtils;

    public NTEDETS_Processor(AccountRepo accountRepo, CryptoUtils cryptoUtils) {
        this.accountRepo = accountRepo;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    public Account process(R2816NTEDETS nte) throws Exception {

        val acOpt = accountRepo.findByPkPrsn(nte.PK_R2816_NINO);

        if(acOpt.isEmpty()) return null;
        val account = acOpt.get();

        val noteTxt = String.join("\n", nte.D2816_NTE_TX_LNEs)
                .replaceAll("\u0000", ""); //https://stackoverflow.com/questions/1347646/postgres-error-on-insert-error-invalid-byte-sequence-for-encoding-utf8-0x0

//        if (!account.isEncrypted()) {
//            account.setNotepad(noteTxt);
//        } else {
//            account.setNotepad(cryptoUtils.encrypt(noteTxt));
//        }
        return null;
    }
}
