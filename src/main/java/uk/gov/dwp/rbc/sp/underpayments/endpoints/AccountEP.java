package uk.gov.dwp.rbc.sp.underpayments.endpoints;

import graphql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.dwp.rbc.sp.underpayments.endpoints.gql.AccountGql;
import uk.gov.dwp.rbc.sp.underpayments.endpoints.gql.GqlQuery;

@CrossOrigin
@RestController
public class AccountEP extends AbstractEP {

    @Autowired
    private AccountGql accountGql;

    //-------------------------------------
    @PostMapping(AccountGql.URL)
    public ExecutionResult processAccountQuery(@RequestBody GqlQuery query) {
        return accountGql.runGqlQuery(query.query);
    }
    @PostMapping(AccountGql.URL+"/_txt")
    public ExecutionResult processAccountTxtQuery(@RequestBody String query) {
        return accountGql.runGqlQuery(query);
    }

}
