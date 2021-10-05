package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter @Setter
public class Fraction implements Comparable<Fraction>{

    private Integer numerator;

    private Integer denominator;

    //---------------------------------------------
    public static Fraction of(int num, int denom){
        val f = new Fraction();
        f.numerator = num;
        f.denominator = denom;
        return f;
    }

    //-----------------------------------------
    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }

    @Override
    public boolean equals(Object frac) {
        return this.compareTo((Fraction) frac) == 0;
    }

    //----------------------------------
    @Override
    public int compareTo(Fraction frac) {
        long t = this.getNumerator() * frac.getDenominator();
        long f = frac.getNumerator() * this.getDenominator();
        int result = 0;
        if(t>f) result = 1;
        else if(f>t) result = -1;
        return result;
    }

    //-----------------------------
    public int toPercentRate(){
        return Math.round(this.getNumerator() / this.getDenominator() * 100);
    }

    //----------------------------------
    public Fraction plus(Fraction other) {
        int n = (numerator * other.getDenominator()) +
                (other.getNumerator() * denominator);
        int d = denominator * other.getDenominator();
        return createNormalised(n, d);
    }

    //----------------------------------
    public Fraction minus(Fraction other) {
        int n = (numerator * other.denominator) -
                (other.numerator * denominator);
        int d = denominator * other.denominator;
        return createNormalised(n, d);
    }

    //----------------------------------------
    public Fraction multiply(Fraction other) {
        int n = numerator * other.numerator;
        int d = denominator * other.denominator;
        return createNormalised(n, d);
    }

    //----------------------------------
    public Fraction divide(Fraction other) {
        int n = numerator * other.getDenominator();
        int d = denominator * other.numerator;
        return createNormalised(n, d);
    }
    //-------------------- Helpers ----------------------
    private int gcd(int numerator, int denominator) {
        if (numerator % denominator == 0) {
            return denominator;
        }
        return gcd(denominator, numerator % denominator);
    }

    private Fraction createNormalised(int n, int d) {
        val result = new Fraction();
        int gcd = gcd(n, d);
        result.numerator = n/gcd;
        result.denominator = d/gcd;
        return result;
    }


}
