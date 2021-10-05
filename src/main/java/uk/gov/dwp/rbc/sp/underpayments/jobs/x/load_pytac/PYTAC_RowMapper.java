package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_pytac;

import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PYTAC_RowMapper implements RowMapper<R2577PYTAC> {
    @Override
    public R2577PYTAC mapRow(ResultSet resultSet, int idx) throws SQLException {

        val pytac = new R2577PYTAC();
        pytac.PK_R2577_NINO = resultSet.getNString("PK_R2577_NINO");
        pytac.D2577_IOP_TP = resultSet.getInt("D2577_IOP_TP");
        pytac.D2577_MOP_INSTR_STRT_DT = resultSet.getInt("D2577_MOP_INSTR_STRT_DT");
        pytac.D2577_MOP_INSTR_END_DT = resultSet.getInt("D2577_MOP_INSTR_END_DT");
        pytac.D2577_PRD_TP = resultSet.getInt("D2577_PRD_TP");
        pytac.D2577_PYT_SUSP_FG = resultSet.getInt("D2577_PYT_SUSP_FG");

        val sb = new StringBuilder();
        for(int i = 1; i <= R2577PYTAC.TOTAL_D2577_PYTAC_D_COLS; i++) {
            val lneColName = String.format("D2577_PYTAC_D%02d", i);
            val lne = resultSet.getNString(lneColName);
            if(!resultSet.wasNull()) {
                sb.append(lne);
            }
        }
        pytac.D2577_PYTAC_D01_27 = sb.toString().trim();

        return pytac;
    }
}
