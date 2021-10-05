package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_ntedets;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class R2816NTEDETS {
    public static final int TOTAL_NTE_TX_LNE_COLS = 17;

    public String PK_R2816_NINO;
    public List<String> D2816_NTE_TX_LNEs = new ArrayList<>();
}
