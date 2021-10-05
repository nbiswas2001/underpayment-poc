package uk.gov.dwp.rbc.sp.underpayments.services.narrator;

import uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm.R2507AW;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm.R2508AWCM;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.SpAward;

import java.util.List;

public class NSpouseData {
    public int num;
    public Account spAccount;
    public List<SpAward> spAwards;
    public List<R2507AW> AWs;
    public List<R2508AWCM> AWCMs;

    public NSpouseData(int idx) {
        num = idx;
    }
}
