package uk.gov.dwp.rbc.sp.underpayments.domain.model.calc;

public enum CalcStep {
    NEW,
    CREATE_ACCOUNT,
    CALC_AC_ELIGIBILITY,
    LOAD_RELNS,
    LOAD_AWARDS,
    CALC_CIRCS_ELIGIBILITY,
    CALC_ENTITLEMENT
}
