package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_actdet;

import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ACTDET_RowMapper implements RowMapper<R2501ACT_DET> {
    @Override
    public R2501ACT_DET mapRow(ResultSet resultSet, int idx) throws SQLException {

        val actdet = new R2501ACT_DET();
        actdet.PKF_R2577_NINO = resultSet.getNString("PKF_R2577_NINO");
        actdet.LAST_D2501_IOP_END_DT = resultSet.getInt("LAST_D2501_IOP_END_DT");

        return actdet;
    }
}
