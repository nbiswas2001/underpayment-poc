package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn;

import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PRLBN_RowMapper implements RowMapper<R2567PRLBN> {
    @Override
    public R2567PRLBN mapRow(ResultSet resultSet, int i) throws SQLException {

        val prlbn = new R2567PRLBN();
        //prlbn.XIAREAID = resultSet.getNString("XIAREAID");
        //prlbn.PKF_R2575_NINO = resultSet.getNString("PKF_R2575_NINO");
        prlbn.PK_SRK_R2567 = resultSet.getLong("PK_SRK_R2567");
        prlbn.D2567_BEN_TP = resultSet.getNString("D2567_BEN_TP");
        prlbn.D2567_BEN_PYDY_TP = resultSet.getInt("D2567_BEN_PYDY_TP");
        prlbn.D2567_PYDY_CONV_IND = resultSet.getInt("D2567_PYDY_CONV_IND");
        prlbn.D2567_PART_WK_RP_IND = resultSet.getInt("D2567_PART_WK_RP_IND");

        return prlbn;
    }
}
