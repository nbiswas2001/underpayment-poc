package uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm;
import lombok.val;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AW_AWCM_RowMapper implements RowMapper<R2507AW_R2508AWCM> {
    @Override
    public R2507AW_R2508AWCM mapRow(ResultSet resultSet, int idx) throws SQLException {

        val awawcm = new R2507AW_R2508AWCM();

        awawcm.PKF_R2575_NINO = resultSet.getNString("PKF_R2575_NINO");
        awawcm.PK_SRK_R2507 = resultSet.getLong("PK_SRK_R2507");
        awawcm.D2507_AW_CRTD_DT = resultSet.getInt("D2507_AW_CRTD_DT");
        awawcm.D2507_AW_STRT_DT = resultSet.getInt("D2507_AW_STRT_DT");
        awawcm.D2507_AW_STAT_TP = resultSet.getInt("D2507_AW_STAT_TP");
        awawcm.PK_SRK_R2508 = resultSet.getLong("PK_SRK_R2508");
        awawcm.PKF_SRK_R2521 = resultSet.getLong("PKF_SRK_R2521");
        awawcm.D2508_AW_CM_TP = resultSet.getInt("D2508_AW_CM_TP");
        awawcm.PKF_SRK_R2567 = resultSet.getLong("PKF_SRK_R2567");
        awawcm.D2508_AW_CM_TP = resultSet.getInt("D2508_AW_CM_TP");
        awawcm.D2508_AW_CM_NO = resultSet.getInt("D2508_AW_CM_NO");
        awawcm.D2508_AW_CM_END_DT = resultSet.getInt("D2508_AW_CM_END_DT");
        awawcm.D2508_AW_CM_STRT_DT = resultSet.getInt("D2508_AW_CM_STRT_DT");
        awawcm.D2508_AW_CM_STAT_TP = resultSet.getInt("D2508_AW_CM_STAT_TP");
        awawcm.D2508_AWCM_BYTES_CNT = resultSet.getInt("D2508_AWCM_BYTES_CNT");
        awawcm.D2508_SUB_AW_CM_CNT = resultSet.getInt("D2508_SUB_AW_CM_CNT");
        awawcm.D2508_GMP_SCM_CNT = resultSet.getInt("D2508_GMP_SCM_CNT");
        awawcm.D2508_AWCMTOAWCM_CNT = resultSet.getInt("D2508_AWCMTOAWCM_CNT");

        val sb = new StringBuilder();
        for(int i=1; i <=17; i++) {
            val colName = String.format("D2508_AWCM_D%02d", i);
            val d = resultSet.getNString(colName);
            if(d != null) sb.append(d);
            else break;
        }

        awawcm.D2508_AWCM_D01_17 = sb.toString().trim();

        if(awawcm.D2508_AWCM_D01_17.length()/2 != awawcm.D2508_AWCM_BYTES_CNT) throw new RuntimeException("Actual bytes don't match expected bytes");

        return awawcm;
    }
}
