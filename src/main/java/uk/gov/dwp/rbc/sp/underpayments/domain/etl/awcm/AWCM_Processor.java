package uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.AC_AWARD_HAS_ERRORS;

@Slf4j
public class AWCM_Processor {

    private SubAWCM_Processor subAWCM_processor;

    private JdbcTemplate jdbc;

    //----------------------------------------------------
    public AWCM_Processor(DataSource pscsDataSource) {
        this.subAWCM_processor = new SubAWCM_Processor();
        this.jdbc = new JdbcTemplate(pscsDataSource);
    }

    //-------------------------------------------------------------------------
    public List<R2507AW> loadAW(Account account) {

        val aw_rowMapper = new AW_RowMapper();

        val awQuery = String.format
                ("select * " +
                    " from R2507AW" +
                    " where PKF_SRK_R2567=%d" +
                    " and D2507_AW_STAT_TP=2" +
                    " order by D2507_AW_STRT_DT asc", account.getPkPrlbn());
        return jdbc.query(awQuery, aw_rowMapper);
    }

    //-------------------------------------------------------------------------
    public List<R2508AWCM> loadAWCM(Account account) {

        val awcm_rowMapper = new AWCM_RowMapper();

        val awcmQuery = String.format
                ("select *" +
                    " from R2508AWCM"+
                    " where D2508_AW_CM_TP=4" +
                    " and (D2508_AW_CM_STAT_TP = 1 or D2508_AW_CM_STAT_TP = 10)"+
                    " and PKF_SRK_R2567=%d" +
                    " order by D2508_AW_CM_STRT_DT asc", account.getPkPrlbn());
        return jdbc.query(awcmQuery, awcm_rowMapper);
    }
}
