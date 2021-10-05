package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.data.annotation.Transient;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.F1010RateEntry;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

@Getter @Setter
public class Rate {

    protected SacType sacType;

    //percent rate = percent * 100
    protected Integer percentRate;

    protected Fraction fraction;

    private int incrementRate = 0;

    @Transient
    public boolean isFraction(){
        return percentRate == null;
    }

    //-----------------------------------------------------------
    public boolean isEqualTo(RateValue value){
        if(isFraction()) return this.fraction.equals(value.fraction);
        else return roundPct(this.percentRate) == value.percentRate;
    }

    //------------------------------------------------------------
    public boolean isLessThan(RateValue value){
        if(isFraction()) return this.fraction.compareTo(value.fraction) < 0;
        else return roundPct(this.percentRate) < value.percentRate;
    }

    //------------------------------------------------------------
    public boolean isAtLeast(RateValue value){
        if(isFraction()) return this.fraction.compareTo(value.fraction) >= 0;
        else return roundPct(this.percentRate) >= value.percentRate;
    }
    //------------------------------------------------------------
    public boolean isAtLeast(Rate rate){
        //Convert fraction to percent rate
        if(isFraction() && rate.isFraction()){
            return this.fraction.compareTo(rate.fraction) >= 0;
        }
        else if(!isFraction() && !rate.isFraction()){
            return roundPct(this.percentRate) >= roundPct(rate.percentRate);
        }
        else { //one is a fraction the other is not
            val thisPctRate = isFraction()? this.fraction.toPercentRate() : this.percentRate;
            val otherPctRate = rate.isFraction() ? rate.fraction.toPercentRate() : rate.percentRate;
            return thisPctRate >= otherPctRate;
        }
    }

    //--------------------------------------
    public void setValueFrom(Rate other) {
        if(other instanceof RateValue) {
            throw new UeException("Argument cannot be a SpRateValue");
        }
        if(other.isFraction()) fraction = other.fraction;
        else percentRate = other.percentRate;
    }

    //-------------------------------------------------
    public int amountFor(F1010RateEntry rateEntry){
        var amtLong = isFraction()?
                Math.round((double) rateEntry.max * fraction.getNumerator() / fraction.getDenominator()) :
                Math.round((double) rateEntry.max * percentRate /100_00);
        return Math.toIntExact(amtLong);
    }

    //----------------------------------------
    private int roundPct(int pctRate){
        return Math.round(pctRate/100)*100;
    }

    //----------------------------
    @Transient
    @JsonIgnore
    public String getValue() {
        if(fraction != null || percentRate != null) {
            return isFraction() ? fraction.toString() : percentRate.toString();
        }
        else return "N/A";
    }

}
