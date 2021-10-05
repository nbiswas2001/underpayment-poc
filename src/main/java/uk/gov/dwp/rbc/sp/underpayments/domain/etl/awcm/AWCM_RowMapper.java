package uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm;
import lombok.val;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AWCM_RowMapper implements RowMapper<R2508AWCM> {
    @Override
    public R2508AWCM mapRow(ResultSet resultSet, int idx) throws SQLException {

        val awcm = new R2508AWCM();

        awcm.XIAREAID = resultSet.getNString("XIAREAID");
        awcm.PKF_R2575_NINO = resultSet.getNString("PKF_R2575_NINO");
        awcm.PK_SRK_R2508 = resultSet.getLong("PK_SRK_R2508");
        awcm.PKF_SRK_R2521 = resultSet.getLong("PKF_SRK_R2521");
        awcm.D2508_AW_CM_TP = resultSet.getInt("D2508_AW_CM_TP");
        awcm.D2508_AW_CM_NO = resultSet.getInt("D2508_AW_CM_NO");
        awcm.D2508_AW_CM_END_DT = resultSet.getInt("D2508_AW_CM_END_DT");
        awcm.D2508_AW_CM_STRT_DT = resultSet.getInt("D2508_AW_CM_STRT_DT");
        awcm.D2508_AW_CM_STAT_TP = resultSet.getInt("D2508_AW_CM_STAT_TP");
        awcm.D2508_AW_CM_SUB_TP_B = resultSet.getInt("D2508_AW_CM_SUB_TP_B");
        awcm.D2508_IMPRT_ASST_FG = resultSet.getInt("D2508_IMPRT_ASST_FG");
        awcm.D2508_INH_SERP_PERC = resultSet.getInt("D2508_INH_SERP_PERC");
        awcm.D2508_ACC_SMY_NO = resultSet.getInt("D2508_ACC_SMY_NO");
        awcm.D2508_IMPRT_TP = resultSet.getInt("D2508_IMPRT_TP");
        awcm.D2508_RESID_COND_DT = resultSet.getInt("D2508_RESID_COND_DT");
        awcm.D2508_AWCM_BYTES_CNT = resultSet.getInt("D2508_AWCM_BYTES_CNT");
        awcm.D2508_SUB_AW_CM_CNT = resultSet.getInt("D2508_SUB_AW_CM_CNT");
        awcm.D2508_GMP_SCM_CNT = resultSet.getInt("D2508_GMP_SCM_CNT");
        awcm.D2508_AWCMTOAWCM_CNT = resultSet.getInt("D2508_AWCMTOAWCM_CNT");

        val sb = new StringBuilder();
        for(int i=1; i <=17; i++) {
            val colName = String.format("D2508_AWCM_D%02d", i);
            val d = resultSet.getNString(colName);
            if(d != null) sb.append(d);
            else break;
        }

        awcm.D2508_AWCM_D01_17 = sb.toString().trim();

        if(awcm.D2508_AWCM_D01_17.length()/2 != awcm.D2508_AWCM_BYTES_CNT) throw new RuntimeException("Actual bytes don't match expected bytes");

        return awcm;
    }
}
