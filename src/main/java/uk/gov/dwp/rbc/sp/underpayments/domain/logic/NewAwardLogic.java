package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.SpAward;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SpSubAwardComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.CAT_A_PRE_97_AP;

@Component
public class NewAwardLogic {

    //------------------------------------------------------------
    private List<SpSubAwardComponent> findAllCatASacs(SpAward award) {
        val result = new ArrayList<SpSubAwardComponent>();

        for(val sac : award.getSubAwardComponents()){
            if(sac instanceof SpSubAwardComponent){
                val peSac = (SpSubAwardComponent) sac;
                if(catATypes.contains(peSac.getRate().getSacType())){
                    result.add(peSac);
                }
            }
        }

        return result;
    }

    //-----------------------------------------------------
    private static final Set<SacType> catATypes = Set.of(
            CAT_A_BASIC,
            CAT_A_POST_02_AP,
            CAT_A_POST_97_AP,
            CAT_A_PRE_97_AP
//            CAT_AB_POST_02_AP,
//            CAT_AB_POST_97_AP,
//            CAT_AB_PRE_97_AP
    );
}
