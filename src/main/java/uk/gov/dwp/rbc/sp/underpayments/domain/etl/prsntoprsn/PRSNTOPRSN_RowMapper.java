package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn;

import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PRSNTOPRSN_RowMapper implements RowMapper<R2576PRSNTOPRSN> {
    @Override
    public R2576PRSNTOPRSN mapRow(ResultSet resultSet, int i) throws SQLException {

        val pp = new R2576PRSNTOPRSN();
        pp.XIAREAID = resultSet.getNString("XIAREAID");
        pp.PK_SRK_R2576 = resultSet.getLong("PK_SRK_R2576");
        pp.PKF_R2575_NINO = resultSet.getNString("PKF_R2575_NINO");
        pp.D2576_NINO_B = resultSet.getNString("D2576_NINO_B");
        if(pp.D2576_NINO_B.isBlank()) pp.D2576_NINO_B = null;
        pp.D2576_REL_TP = resultSet.getInt("D2576_REL_TP");
        pp.D2576_REL_STRT_DT = resultSet.getInt("D2576_REL_STRT_DT");
        pp.D2576_REL_END_DT = resultSet.getInt("D2576_REL_END_DT");
        pp.D2576_REL_END_RSN_TP = resultSet.getInt("D2576_REL_END_RSN_TP");
        pp.D2576_VFD_REL_STRT_TP = resultSet.getInt("D2576_VFD_REL_STRT_TP");
        pp.D2576_VFD_REL_END_TP = resultSet.getInt("D2576_VFD_REL_END_TP");

        return pp;
    }
}
