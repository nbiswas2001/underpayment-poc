package uk.gov.dwp.rbc.sp.underpayments.services.narrator;

public class NListItem<T> {
    public NListItem(int index, T item) {
        this.index = index;
        this.item = item;
    }
    public int index;
    public T item;
}
