package uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.SpAward;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.utils.PscsUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.AC_AWARD_HAS_ERRORS;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.AW_NO_START_DATE;

@Slf4j
public class AW_AWCM_Processor implements ItemProcessor<Account, Account> {

    private SubAWCM_Processor subAWCM_processor;

    private JdbcTemplate jdbc;

    //----------------------------------------------------
    public AW_AWCM_Processor(DataSource pscsDataSource) {
        this.subAWCM_processor = new SubAWCM_Processor();
        this.jdbc = new JdbcTemplate(pscsDataSource);
    }

    //----------------------------------------------------
    @Override
    public Account process(Account account) throws Exception {

        account.tryLoad(acProblems -> {

            val awawcmList = loadAW_AWCM(account);
            val awards = new ArrayList<SpAward>();

            for (val awawcm : awawcmList) {
                val award = new SpAward();
                award.tryLoad(awErr -> {
                    if(awawcm.D2507_AW_STRT_DT == 0){
                        awErr.errors().set(AW_NO_START_DATE);
                    }
                    else {
                        award.setStartDate(PscsUtils.toDate(awawcm.D2507_AW_STRT_DT));
                        award.setStatus(SpAward.Status.PAYABLE);
                        award.setAwcmNum(awawcm.D2508_AW_CM_NO);
                        award.setPkPrlbn(awawcm.PKF_SRK_R2567);
                        award.setPkAw(awawcm.PK_SRK_R2507);
                        award.setPkAwcm(awawcm.PK_SRK_R2508);
                        award.setPkClm(awawcm.PKF_SRK_R2521);
                        award.setSubAwardComponents(subAWCM_processor.process(awawcm, awErr));
                    }
                });
                awards.add(award);
                if (award.getProblems().getHasErrors()) {
                    acProblems.errors().set(AC_AWARD_HAS_ERRORS);
                }
            }

            if (awards.size() > 0) {
                account.setSpStartDate(awards.get(0).getStartDate());
                account.setAwards(awards);
            }

        });

        if (account.getProblems().getHasErrors()) {
            account.getCalcResult().setCode(CalcResult.Code.DATA_ERROR);
        }

        account.getCalcResult().setAwardsLoaded(true);
        return account;
    }


    //-------------------------------------------------------------------------
    public List<R2507AW_R2508AWCM> loadAW_AWCM(Account account) {

        val awcm_rowMapper = new AW_AWCM_RowMapper();

        val awcmQuery = String.format
                ("select PK_SRK_R2507,D2507_AW_STRT_DT,D2507_AW_CRTD_DT,D2507_AW_STAT_TP,AWCM.*" +
                    " from R2507AW AW" +
                    " left join (" +
                    "    select * from R2508AWCM" +
                    "    where D2508_AW_CM_TP=4" +
                    "    and (D2508_AW_CM_STAT_TP = 1 or D2508_AW_CM_STAT_TP = 10)" +
                    "    ) AWCM" +
                    " on AW.PKF_SRK_R2567 = AWCM.PKF_SRK_R2567" +
                    " where D2507_AW_STAT_TP=2" +
                    " and (  AW.D2507_AW_CM_NO_01=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_02=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_03=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_04=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_05=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_06=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_07=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_08=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_09=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_10=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_11=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_12=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_13=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_14=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_15=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_16=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_17=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_18=D2508_AW_CM_NO" +
                    "     or AW.D2507_AW_CM_NO_19=D2508_AW_CM_NO or AW.D2507_AW_CM_NO_20=D2508_AW_CM_NO)" +
                    " and AW.PKF_SRK_R2567=%d" +
                    " order by D2507_AW_STRT_DT asc", account.getPkPrlbn());
        return jdbc.query(awcmQuery, awcm_rowMapper);
    }

}
