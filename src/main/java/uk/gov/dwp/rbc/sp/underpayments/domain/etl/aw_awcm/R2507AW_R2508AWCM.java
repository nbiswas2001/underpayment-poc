package uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm;

import lombok.ToString;

@ToString
public class R2507AW_R2508AWCM {

    //AW
    public Long PK_SRK_R2507;
    public int D2507_AW_CRTD_DT;
    public int D2507_AW_STRT_DT;
    public int D2507_AW_STAT_TP;

    //AWCM
    public String PKF_R2575_NINO;
    public Long PK_SRK_R2508;
    public Long PKF_SRK_R2567;
    public Long PKF_SRK_R2521;
    public int D2508_AW_CM_TP;
    public int D2508_AW_CM_NO;
    public int D2508_AW_CM_END_DT;
    public int D2508_AW_CM_STRT_DT;
    public int D2508_AW_CM_STAT_TP;

    // SACs
    public int D2508_AWCM_BYTES_CNT;
    public int D2508_SUB_AW_CM_CNT;
    public int D2508_GMP_SCM_CNT;
    public int D2508_AWCMTOAWCM_CNT;
    public String D2508_AWCM_D01_17;
}
