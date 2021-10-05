package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BankAccount {

    public String name;
    public String number;
    public String sortCode;
    public String bsocCode;
    public String bic;
    public String iban;
    public String countryCode;

}
