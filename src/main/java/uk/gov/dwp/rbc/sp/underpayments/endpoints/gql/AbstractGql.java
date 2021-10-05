package uk.gov.dwp.rbc.sp.underpayments.endpoints.gql;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import uk.gov.dwp.rbc.sp.underpayments.utils.ID;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public abstract class AbstractGql {

    protected static SchemaParser schemaParser = new SchemaParser();
    protected static SchemaGenerator schemaGenerator = new SchemaGenerator();

    private ResourceLoader resourceLoader;

    public AbstractGql(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected abstract String schemaName();

    protected abstract RuntimeWiring.Builder wiringBuilder();

    protected GraphQL graphQL;

    //----------------------------------------------------
    private String getSchemaSource(){
        val schemaFilename = "classpath:gql.schema/"+ schemaName()+".graphql";
        val res = resourceLoader.getResource(schemaFilename);
        try {
            return CharStreams.toString(new InputStreamReader(res.getInputStream(), Charsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't find file "+ schemaFilename +" in classpath", e);
        }
    }

    //----------------------------------------------------
    @PostConstruct
    public void init(){

        val typeRegistry = schemaParser.parse(getSchemaSource());

        val wiringBuilder = wiringBuilder();
        val wiring = wiringBuilder
                        .scalar(ExtendedScalars.Date)
                        .scalar(ExtendedScalars.DateTime)
                        .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                        .build();

        val schema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    //--------------------------------------------------------------
    public ExecutionResult runGqlQuery(String query) {
        val executionInput = ExecutionInput.newExecutionInput()
                .query(query).build();

        return graphQL.execute(executionInput);
    }

    //-----------------------------------------------------------------------
    protected PageRequest getPageRequest(DataFetchingEnvironment environment) {
        var pgNum = getArg(environment, "pageNum", 0);
        var pgSize = getArg(environment, "pageSize", GqlPageResponse.DEFAULT_PAGE_SIZE);
        if(pgSize > GqlPageResponse.MAX_PAGE_SIZE) pgSize = GqlPageResponse.MAX_PAGE_SIZE;
        return PageRequest.of(pgNum, pgSize);
    }

    //---------------------------------------------------------------------------
    protected Long getIdArg(DataFetchingEnvironment environment, String name){
        val s = getArg(environment, name, String.class);
        return ID.from(s);
    }

    protected <T> T getArg(DataFetchingEnvironment environment, String name, Class<T> clazz){
        if(!environment.getArguments().containsKey(name)) {
            throw new RuntimeException("Argument "+name+" is missing in query");
        }
        else{
            return (T) environment.getArguments().get(name);
        }
    }

    protected <T> T getArg(DataFetchingEnvironment environment, String name, T defaultValue){
        if(!environment.getArguments().containsKey(name)) return defaultValue;
        else return (T) environment.getArguments().get(name);
    }
}
