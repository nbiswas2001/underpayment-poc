package uk.gov.dwp.rbc.sp.underpayments.endpoints.gql;

import graphql.TypeResolutionEnvironment;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.*;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Account.AgeCategory.OVER_80;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Account.AgeCategory.SPA;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Code.*;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult.Reason.*;

@Slf4j
@Component
public class AccountGql extends AbstractGql {

    @Override
    protected String schemaName() {
        return "account";
    }


    private AccountRepo accountRepo;

    private CryptoUtils cryptoUtils;

    private AppConfig config;

    @Autowired
    public AccountGql(AccountRepo accountRepo,
                      ResourceLoader resourceLoader,
                      CryptoUtils cryptoUtils,
                      AppConfig config) {
        super(resourceLoader);
        this.accountRepo = accountRepo;
        this.cryptoUtils = cryptoUtils;
        this.config = config;
    }

    public static final String URL = "/queries/account";

    //--------------------------------------------------------------------------------

    @Override
    protected RuntimeWiring.Builder wiringBuilder() {

        val wiringBuilder = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("all", new AllDF())
                        .dataFetcher("withCitizenKey", new WithCitizenKeyDF())
                        .dataFetcher("withId", new WithIdDF())
                )
                .type("SubAwardComponent", tw -> tw.typeResolver(subAwardComponentResolver));

        return wiringBuilder;
    }

    //--------------------------------------------------------------------------------
    public class AllDF implements DataFetcher<GqlPageResponse<Account>> {

        @Override
        public GqlPageResponse<Account> get(DataFetchingEnvironment environment) throws Exception {
            val pgRequest = getPageRequest(environment);
            val filter = getArg(environment, "filter", "noFilter");

            Page<Account> page;

            switch (filter){
                case "ineligibleSpa": page = accountRepo.withAgeCategoryAndResultCode(pgRequest, s(SPA), s(INELIGIBLE)); break;
                case "ineligible80": page = accountRepo.withAgeCategoryAndResultCode(pgRequest, s(OVER_80), s(INELIGIBLE)); break;
                case "eligibleBL": page = accountRepo.withResultCodeAndReason(pgRequest, s(ELIGIBLE), s(CAT_BL)); break;
                case "entitledBL": page = accountRepo.withResultCodeAndReason(pgRequest, s(ENTITLED), s(CAT_BL)); break;
                case "eligibleD": page = accountRepo.withResultCodeAndReason(pgRequest, s(ELIGIBLE), s(CAT_D)); break;
                case "entitledD": page = accountRepo.withResultCodeAndReason(pgRequest, s(ENTITLED), s(CAT_D)); break;
                case "eligibleBLplusD": page = accountRepo.withResultCodeAndReason(pgRequest, s(ELIGIBLE), s(CAT_BL_AND_D)); break;
                case "entitledBLplusD": page = accountRepo.withResultCodeAndReason(pgRequest, s(ENTITLED), s(CAT_BL_AND_D)); break;
                case "withErrorsSPA":  page = accountRepo.withAgeCategoryAndResultCode(pgRequest, s(SPA), s(DATA_ERROR)); break;
                case "withErrors": page = accountRepo.withErrors(pgRequest); break;
                case "withWarnings": page = accountRepo.withWarnings(pgRequest); break;
                default: page = accountRepo.findAll(pgRequest); break;
            }
            return GqlPageResponse.of(page);
        }
    }

    private static String s(Object o){
        return o.toString();
    }

    //-------------------------------------------------------------------------------
    public class WithCitizenKeyDF implements DataFetcher<GqlResponse<Account>> {

        @Override
        public GqlResponse<Account> get(DataFetchingEnvironment environment) throws Exception {
            val ck = cryptoUtils.toCitizenKey(getArg(environment, "citizenKey", String.class));
            val accOpt = accountRepo.findByCitizenKey(ck);
            return GqlResponse.of(accOpt);
        }
    }
    //-------------------------------------------------------------------------------
    public class WithIdDF implements DataFetcher<GqlResponse<Account>> {

        @Override
        public GqlResponse<Account> get(DataFetchingEnvironment environment) throws Exception {
            val accOpt = accountRepo.findById(getArg(environment, "id", String.class));
            return GqlResponse.of(accOpt);
        }
    }


    //----------------------------------------------------------
    private TypeResolver subAwardComponentResolver = new TypeResolver() {
        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            Object javaObject = env.getObject();
            if (javaObject instanceof SpSubAwardComponent) {
                return env.getSchema().getObjectType("PESubAwcm");
            } else if (javaObject instanceof GmpSubAwcm) {
                return env.getSchema().getObjectType("GmpSubAwcm");
            } else {
                return env.getSchema().getObjectType("Awcm2AwcmLink");
            }
        }
    };
}
