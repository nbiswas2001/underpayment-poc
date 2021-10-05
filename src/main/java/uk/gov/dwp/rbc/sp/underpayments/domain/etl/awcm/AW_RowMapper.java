package uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm;

import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AW_RowMapper implements RowMapper<R2507AW> {
    @Override
    public R2507AW mapRow(ResultSet resultSet, int idx) throws SQLException {

        val aw = new R2507AW();
        aw.D2507_AW_CRTD_DT = resultSet.getInt("D2507_AW_CRTD_DT");
        aw.D2507_AW_STRT_DT = resultSet.getInt("D2507_AW_STRT_DT");
        aw.D2507_AW_STAT_TP = resultSet.getInt("D2507_AW_STAT_TP");
        for(int i=1; i<=20; i++) {

            if(!resultSet.wasNull()) {
                val numColName = String.format("D2507_AW_CM_NO_%02d", i);
                val tpColName = String.format("D2507_AW_CM_TP_%02d", i);
                val awcmTp = resultSet.getInt(tpColName);
                if(awcmTp == 4) {
                    aw.PE_AWCM_NO = resultSet.getInt(numColName);
                    break;
                }
            }
        }
        return aw;
    }
}
