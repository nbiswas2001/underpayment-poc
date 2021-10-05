package uk.gov.dwp.rbc.sp.underpayments.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Address {
    private List<String> lines = new ArrayList<>();
    private String postCode;
    private String countryCode;
}
