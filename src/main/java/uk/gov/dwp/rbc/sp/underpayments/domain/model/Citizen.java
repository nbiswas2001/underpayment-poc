package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Citizen {

    private String nino;
    private String ninoSuffix;
    private Name name;
    private Address contactAddress;
    private BankAccount bankAccount;
    private BankAccount bankAccount2;
}
