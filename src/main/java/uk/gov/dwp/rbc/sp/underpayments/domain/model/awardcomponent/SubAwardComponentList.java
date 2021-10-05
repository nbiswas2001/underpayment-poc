package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class SubAwardComponentList {

    private List<SubAwardComponent> subAwardComponents = new ArrayList<>();

}
