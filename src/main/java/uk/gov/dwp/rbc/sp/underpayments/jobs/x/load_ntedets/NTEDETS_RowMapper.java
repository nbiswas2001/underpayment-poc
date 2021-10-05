package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_ntedets;


import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NTEDETS_RowMapper implements RowMapper<R2816NTEDETS> {
    @Override
    public R2816NTEDETS mapRow(ResultSet resultSet, int idx) throws SQLException {
        val nte = new R2816NTEDETS();
        nte.PK_R2816_NINO = resultSet.getNString("PK_R2816_NINO");
        for(int i = 1; i <= R2816NTEDETS.TOTAL_NTE_TX_LNE_COLS; i++) {
            val lneColName = String.format("D2816_NTE_TX_LNE_%02d", i);
            val lne = resultSet.getNString(lneColName);
            if(!resultSet.wasNull()) {
                nte.D2816_NTE_TX_LNEs.add(lne);
            }
        }
        return nte;
    }
}
