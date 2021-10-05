package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.*;

public class RateValue extends Rate {

    public RateValue(SacType sacType, int percentRate, Fraction fraction){
        this.sacType = sacType;
        this.percentRate = percentRate;
        this.fraction = fraction;
    }

    @Override
    public boolean isFraction(){
        throw new UnsupportedOperationException("isFraction() is invalid on SpRateValue");
    }

    public static final RateValue minCatAContribution = new RateValue(CAT_A_BASIC, 25_00, Fraction.of(1, 30));

    public static final Map<SacType, RateValue> maxAwardThresholds = Map.of(
            CAT_A_BASIC, new RateValue(CAT_A_BASIC,60_00, Fraction.of(18, 30)),
            CAT_B_BASIC, new RateValue(CAT_B_BASIC,60_00, Fraction.of(18, 30)),
            COMPOSITE_AB_BC, new RateValue(COMPOSITE_AB_BC,100_00, Fraction.of(30, 30)),
            COMPOSITE_ABL_BC, new RateValue(COMPOSITE_ABL_BC,100_00, Fraction.of(30, 30)),
            CAT_BL_BASIC, new RateValue(CAT_BL_BASIC,100_00, Fraction.of(30, 30)),
            CAT_D_BASIC, new RateValue(CAT_D_BASIC,100_00, Fraction.of(30, 30))
    );


}
