package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.val;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;

import java.util.Arrays;
import java.util.BitSet;

public abstract class AbstractFlags<T extends Enum> {

    private BitSet bitSet = new BitSet(32);

    protected abstract T[] values();

    public AbstractFlags(Integer data){
        data = data == null? 0 : data;
        bitSet = BitSet.valueOf(new long[]{data});
    }

    //---------------------------------------
    public void set(T... flags){
        Arrays.stream(flags).forEach(f -> {
            bitSet.set(f.ordinal());
        });
    }

    //---------------------------------------
    public void unset(T... flags){
        Arrays.stream(flags).forEach(f -> {
            bitSet.clear(f.ordinal());
        });
    }

    //---------------------------------------
    public boolean isSet(T flag) {
        return bitSet.get(flag.ordinal());
    }

    //---------------------------------------
    public int getData(){
        val array = bitSet.toLongArray();
        if(array.length == 0) return 0;
        else return Math.toIntExact(bitSet.toLongArray()[0]);
    }

    //---------------------------------------
    public boolean isAllSet(T... flags) {
        return Arrays.stream(flags).allMatch(e -> bitSet.get(e.ordinal()));
    }

    //---------------------------------------
    public boolean isAnySet(T... flags) {
        return Arrays.stream(flags).anyMatch(e -> bitSet.get(e.ordinal()));
    }

    //---------------------------------------
    public boolean isAtLeastOneSet() {
        return !bitSet.isEmpty();
    }

    //---------------------------------------
    public static <T> String getHeaderRow(T[] flags) {
        val sb = new StringBuilder();
        val flagValues = flags;
        sb.append(flagValues[0].toString());
        for(int i=1; i < flagValues.length; i++){
            sb.append("|").append(flagValues[i].toString());
        }
        return sb.toString();
    }

    //---------------------------------------
    public String getDataRow() {
        val sb = new StringBuilder();
        val flagValues = values();
        var r = isSet(flagValues[0])? "Y": "N";
        sb.append(r);
        for(int i=1; i < flagValues.length; i++){
            r = isSet(flagValues[i])? "Y": "N";
            sb.append("|").append(r);
        }
        return sb.toString();
    }

    //---------------------------------------
    @Override
    public String toString() {
        val sb = new StringBuilder();
        for(val f : values()){
            val r = isSet(f)? "Y" : "N";
            sb.append(f.toString()).append("=").append(r).append("\n");
        }
        return sb.toString();
    }
}
