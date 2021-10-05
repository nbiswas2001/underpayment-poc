package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn;


import lombok.val;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PRSN_RowMapper implements RowMapper<R2575PRSN> {

    private static final PRLBN_RowMapper prlbn_RowMapper = new PRLBN_RowMapper();

    @Override
    public R2575PRSN mapRow(ResultSet resultSet, int i) throws SQLException {
        val p = new R2575PRSN();
        p.XIAREAID = resultSet.getNString("XIAREAID");
        p.PK_R2575_NINO = resultSet.getNString("PK_R2575_NINO");
        p.D2575_NINO_SUFFIX = resultSet.getNString("D2575_NINO_SUFFIX");
        p.D2575_BTH_DT = resultSet.getInt("D2575_BTH_DT");
        p.D2575_BTH_DT_VFD_TP = resultSet.getInt("D2575_BTH_DT_VFD_TP");
        p.D2575_DTH_DT = resultSet.getInt("D2575_DTH_DT");
        p.D2575_DTH_DT_VFD_TP = resultSet.getInt("D2575_DTH_DT_VFD_TP");
        p.D2575_SEX_FG = resultSet.getNString("D2575_SEX_FG");
        p.D2575_PST_CDE = resultSet.getNString("D2575_PST_CDE");
        p.D2575_ACC_STAT_IND = resultSet.getNString("D2575_ACC_STAT_IND");
        p.D2575_RESID_CTRY_CDE_NO = resultSet.getInt("D2575_RESID_CTRY_CDE_NO");
        p.D2575_PERSON_BYTE_CNT = resultSet.getInt("D2575_PERSON_BYTE_CNT");
        p.D2575_TITLE_TX_CNT = resultSet.getInt("D2575_TITLE_TX_CNT");
        p.D2575_FRST_FNME_TX_CNT = resultSet.getInt("D2575_FRST_FNME_TX_CNT");
        p.D2575_SUBSQ_FNME_TX_CNT = resultSet.getInt("D2575_SUBSQ_FNME_TX_CNT");
        p.D2575_SNME_TX_CNT = resultSet.getInt("D2575_SNME_TX_CNT");
        p.D2575_RQSTD_TITLE_TX_CNT = resultSet.getInt("D2575_RQSTD_TITLE_TX_CNT");
        p.D2575_FRST_ADD_LNE_CNT = resultSet.getInt("D2575_FRST_ADD_LNE_CNT");
        p.D2575_SCND_ADD_LNE_CNT = resultSet.getInt("D2575_SCND_ADD_LNE_CNT");
        p.D2575_THRD_ADD_LNE_CNT = resultSet.getInt("D2575_THRD_ADD_LNE_CNT");
        p.D2575_FRTH_ADD_LNE_CNT = resultSet.getInt("D2575_FRTH_ADD_LNE_CNT");
        p.D2575_PERSON_D603 = resultSet.getNString("D2575_PERSON_D603");

        p.RELN_CNT = resultSet.getInt("RELN_CNT");
        p.PC_CNT = resultSet.getInt("PC_CNT");
        p.FRZN = resultSet.getInt("FRZN");

        p.prlbn = prlbn_RowMapper.mapRow(resultSet, i);

        return p;
    }
}
