package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

public enum SacType {

    CAT_A_BASIC (1),
    CAT_B_BASIC (2),
    UND_TITLE_IBST_H (3),
    CAT_BL_BASIC (4),
    CAT_DL (5),
    CAT_C_BASIC (6),
    CAT_CL_BASIC (7),
    CAT_D_BASIC (8),
    CAT_A_PRE_97_AP (9),
    CAT_B_PRE_97_AP (10),
    COMPOSITE_AB_BC (11),
    COMPOSITE_ABL_BC (12, 5),
    CAT_AB_PRE_97_AP (13),
    GRB (14),
    DEP_CHILD (15),
    DEP_SPOUSE (16),
    AGE_ADDITION (17),
    IVA_LOW (18),
    IVA_MID (19),
    IVA_HIGH (20),
    CSA (21),
    AA_LOW (22),
    AA_HIGH (23),
    COMPOSITE_GRB (24),
    SPARE (25),
    ONE_PARENT_BENEFIT (93),
    DEP_HOUSEKEEPER (131),
    CAT_A_POST_97_AP (164),
    CAT_B_POST_97_AP (165),
    CAT_AB_POST_97_AP (166),
    SHARED_AP (186),
    SHARED_AP_INCS (187),
    CAT_AB_POST_02_AP (189),
    CAT_A_POST_02_AP (192),
    CAT_B_POST_02_AP (193),
    IAA_LOW (194),
    IAA_HIGH (195),
    NSP_INPUT (210),
    PROT_PMT (211),
    RRE_LOW (212),
    RRE_HIGH (213),
    PROT_PMT_INH (214),
    SP_TOP_UP_ESP_INH (215),
    AP_INH (216),
    GRB_INH (217),
    PSOD_CREDIT (218),
    PSOD_DEBIT (219),
    NSP_ESP_INPUT (220),
    PROT_PMT_ESP (221),
    AP_ESP_INH (222),
    GRB_ESP_INH (223),
    OLD_RULES_AMT (224),
    NEW_RULES_AMT (225),
    MQP_SATISFIED (226),
    BP_ESP_INH (227),
    NSP_PAYABLE (235),
    PROT_PMT_PAYABLE (236),
    ESP_PAYABLE (237),
    ESP_INH_PAYABLE (238),
    //Only on F1010
    BC_INCS(-1, 12);

    //---------------------------------------------
    SacType(final int code, final int f1010Code) {
        this.code = code;
        this.f1010Code = f1010Code;
    }

    SacType(final int code) {
        this.code = code;
        this.f1010Code = code;
    }

    //----------------------
    private final int code;
    private final int f1010Code;
    public int getCode() { return code; }
    public int getF1010Code() { return f1010Code; }
}
