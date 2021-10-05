package uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm;

import lombok.ToString;

@ToString
public class R2508AWCM {

    //Common
    public String XIAREAID;
    public String PKF_R2575_NINO;
    public Long PK_SRK_R2508;
    public Long PKF_SRK_R2521; //Claim id
    public int D2508_AW_CM_TP; //Discriminator
    public int D2508_AW_CM_NO;
    public int D2508_AW_CM_END_DT;
    public int D2508_AW_CM_STRT_DT;
    public int D2508_AW_CM_STAT_TP;

    // Personal Entitlement (PE)
    public int D2508_AW_CM_SUB_TP_B;
    public int D2508_IMPRT_ASST_FG;
    public int D2508_INH_SERP_PERC;
    public int D2508_ACC_SMY_NO;
    public int D2508_IMPRT_TP;
    public int D2508_RESID_COND_DT;

    // Sub components
    public int D2508_AWCM_BYTES_CNT;
    public int D2508_SUB_AW_CM_CNT;
    public int D2508_GMP_SCM_CNT;
    public int D2508_AWCMTOAWCM_CNT;
    public String D2508_AWCM_D01_17;
}
