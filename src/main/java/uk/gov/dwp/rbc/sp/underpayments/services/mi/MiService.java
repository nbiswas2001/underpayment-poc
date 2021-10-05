package uk.gov.dwp.rbc.sp.underpayments.services.mi;

import lombok.val;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

@Component
public class MiService {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoHelper mongoHelper;

    //--------------------------------------------------
    public OverviewRec getOverview() {

        mongoHelper.ensureIndicesForReports();
        val ov =
                OverviewRec.total()
                    .with("With Errors", "{'problems.hasErrors':true}").endWith()
                    .with("Too Complex", "{'calcResult.code':'TOO_COMPLEX'}").endWith()
                    .with("Ineligible","{'calcResult.code':'INELIGIBLE'}").endWith()
                    .with("Eligible: BL","{'calcResult.reason':'CAT_BL'}").endWith()
                    .with("Eligible: D", "{'calcResult.reason':'CAT_D'}").endWith()
                    .with("Eligible: D + BL", "{'calcResult.reason':'CAT_BL_AND_D'}").endWith();


        populateOverviewRec(ov);

        return ov;
    }

    //-------------------------------------------------
    private void populateOverviewRec(OverviewRec ov){
        try {
            ov.count = mongoTemplate.count(ov.query(), "account");
        } catch (Exception e){
            throw new UeException("Failed to execute "+ov, e);
        }
        for(val compOv : ov.components){
            populateOverviewRec(compOv);
        }
    }

}
