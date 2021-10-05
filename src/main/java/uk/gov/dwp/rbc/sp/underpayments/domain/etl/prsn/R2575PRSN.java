package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn;

import lombok.ToString;

@ToString
public class R2575PRSN {
    public String XIAREAID;
    public String PK_R2575_NINO;
    public String D2575_NINO_SUFFIX;
    public int D2575_BTH_DT;
    public int D2575_BTH_DT_VFD_TP;
    public int D2575_DTH_DT;
    public int D2575_DTH_DT_VFD_TP;
    public String D2575_SEX_FG;
    public String D2575_PST_CDE;
    public String D2575_ACC_STAT_IND;
    public int D2575_RESID_CTRY_CDE_NO;
    public int D2575_PERSON_BYTE_CNT;
    public int D2575_TITLE_TX_CNT;
    public int D2575_FRST_FNME_TX_CNT;
    public int D2575_SUBSQ_FNME_TX_CNT;
    public int D2575_SNME_TX_CNT;
    public int D2575_RQSTD_TITLE_TX_CNT;
    public int D2575_FRST_ADD_LNE_CNT;
    public int D2575_SCND_ADD_LNE_CNT;
    public int D2575_THRD_ADD_LNE_CNT;
    public int D2575_FRTH_ADD_LNE_CNT;
    public String D2575_PERSON_D603;
    public int RELN_CNT;
    public int PC_CNT;
    public int FRZN;
    public R2567PRLBN prlbn;
}

