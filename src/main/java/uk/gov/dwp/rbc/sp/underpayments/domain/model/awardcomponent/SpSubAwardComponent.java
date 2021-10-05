package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SpSubAwardComponent extends SubAwardComponent{

    private Rate rate;

    private int flag1; //Not sure what this is for

    @Override
    public String getType() {
        return "PE";
    }
}
