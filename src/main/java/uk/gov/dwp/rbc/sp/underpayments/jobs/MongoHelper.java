package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;

import java.util.HashMap;

@Component
@Slf4j
public class MongoHelper {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    AppConfig appConfig;

    //--------------------------------------------------------------------------------------------
    public MongoItemReader<Account> getAccountReaderForStep(CalcStep step, String schema, String partitionKey){

        String query = null;
        switch (step){
            case LOAD_RELNS:
                query= String.format("{ schema:'%s', xiAreaId: %s, 'calcResult.relnsLoaded': false }", schema, partitionKey);
                break;
            case LOAD_AWARDS:
                query= String.format("{ schema:'%s', xiAreaId: %s, 'calcResult.awardsLoaded': false }", schema, partitionKey);
                break;
            case CALC_AC_ELIGIBILITY:
                query= String.format("{ xiAreaId: '%s', 'problems.hasErrors': false, 'calcResult.acEligCalculated': false }",partitionKey);
                break;
            case CALC_CIRCS_ELIGIBILITY:
                query = String.format("{ schema:'%s', partition: %s, 'problems.hasErrors': false, 'calcResult.circsEligCalculated': false, 'calcResult.maybeEligible': true }", schema, partitionKey);
                break;
            case CALC_ENTITLEMENT:
                query = String.format("{ schema:'%s', partition: %s, 'calcResult.eligible': true }", schema, partitionKey);
                break;
        }

        val sort = new HashMap<String, Sort.Direction>();
        sort.put("pkPrsn", Sort.Direction.ASC);

        return new MongoItemReaderBuilder<Account>()
                .template(mongoTemplate)
                .collection("account")
                .fields("{}")
                .jsonQuery(query)
                .saveState(false)
                .sorts(sort)
                .targetType(Account.class)
                .pageSize(appConfig.getBatchJobChunkSize())
                .build();

    }

    //----------------------------------------------
    public void ensureIndicesForStep(CalcStep step){
        log.info("Ensuring mongo indices");
        switch (step){
            case CREATE_ACCOUNT:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new Index()
                                .on("pkPrsn", Sort.Direction.ASC)
                                .unique());
                break;

            case LOAD_RELNS:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new CompoundIndexDefinition(new Document()
                                .append("schema", 1)
                                .append("xiAreaId", 1)
                                .append("calcResult.relnsLoaded", 1)
                                .append("_id", 1)));
                break;

            case LOAD_AWARDS:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new CompoundIndexDefinition(new Document()
                                .append("schema", 1)
                                .append("xiAreaId", 1)
                                .append("calcResult.awardsLoaded", 1)
                                .append("_id", 1)));
                break;

            case CALC_AC_ELIGIBILITY:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new CompoundIndexDefinition(new Document()
                                .append("xiAreaId", 1)
                                .append("problems.hasErrors", 1)
                                .append("calcResult.acEligCalculated", 1)
                                .append("_id", 1)));
                break;

            case CALC_CIRCS_ELIGIBILITY:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new CompoundIndexDefinition(new Document()
                                .append("schema", 1)
                                .append("partition", 1)
                                .append("problems.hasErrors", 1)
                                .append("calcResult.circsEligCalculated", 1)
                                .append("calcResult.maybeEligible", 1)
                                .append("_id", 1)));
                break;

            case CALC_ENTITLEMENT:
                mongoTemplate.indexOps("account")
                        .ensureIndex(new CompoundIndexDefinition(new Document()
                                .append("schema", 1)
                                .append("partition", 1)
                                .append("calcResult.eligible", 1)
                                .append("_id", 1)));
                break;
        }
    }

    //--------------------------------------
    public void ensureIndicesForReports() {

        mongoTemplate.indexOps("account")
                .ensureIndex(new Index()
                        .on("citizenKey", Sort.Direction.ASC));

        mongoTemplate.indexOps("account")
                .ensureIndex(new CompoundIndexDefinition(new Document()
                        .append("calcResult.code", 1)
                        .append("calcResult.reason", 1)));

        mongoTemplate.indexOps("account")
                .ensureIndex(new Index()
                        .on("problems.hasErrors", Sort.Direction.ASC));

        mongoTemplate.indexOps("account")
                .ensureIndex(new Index()
                        .on("calcResult.needsToClaim", Sort.Direction.ASC));

    }

}
