package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Data;

@Data
public class Name {
    private String title;
    private String firstName;
    private String middleNames;
    private String surname;
}
