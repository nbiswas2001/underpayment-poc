package uk.gov.dwp.rbc.sp.underpayments.jobs.create_ac;

import lombok.val;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.PRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.PRSN_RowMapper;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.R2575PRSN;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.SpaLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.XiAreaIdParitioner;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

public class CreateAccount_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    AppConfig appConfig;

    @Autowired
    SpaLogic spaLogic;

    @Autowired
    PscsData pscsData;

    //---------------------------------------------------------------
    @Bean
    public Step createAccount_Step(Step createAccount_PStep,
                                   XiAreaIdParitioner xiAreaIdPartitioner,
                                   TaskExecutor taskExecutor,
                                   StepExecutionListener stepListener) {
        return stepBuilderFactory.get("createAccount")
                .partitioner(createAccount_PStep.getName(), xiAreaIdPartitioner)
                .step(createAccount_PStep)
                .gridSize(appConfig.getBatchJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step createAccount_PStep(JdbcPagingItemReader<R2575PRSN> PRSN_Reader,
                                MongoItemWriter<Account> accountWriter,
                                ChunkListener chunkListener,
                                PRSN_Processor prsn_processor) {
        return stepBuilderFactory.get("createAccount_P")
                .<R2575PRSN, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(PRSN_Reader)
                .processor(prsn_processor)
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //---------------------------
    @Bean
    @StepScope
    PRSN_Processor prsn_processor(@Value("#{stepExecutionContext['schema']}") String schema){
        return new PRSN_Processor(cryptoUtils,
                spaLogic,
                appConfig,
                pscsData.getDataSource(schema));
    }

    //---------------------------------
    @Bean
    @StepScope
    public JdbcPagingItemReader<R2575PRSN> PRSN_Reader(
            PagingQueryProvider PRSN_QueryProvider,
            @Value("#{stepExecutionContext['schema']}") String schema) {

        return new JdbcPagingItemReaderBuilder<R2575PRSN>()
                .name("PRSN_Reader")
                .dataSource(pscsData.getDataSource(schema))
                .queryProvider(PRSN_QueryProvider)
                .rowMapper(new PRSN_RowMapper())
                .pageSize(appConfig.getBatchJobChunkSize())
                .build();
    }

    //---------------------------------------------
    @Bean
    @StepScope
    public PagingQueryProvider PRSN_QueryProvider(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {

        val ds = pscsData.getDataSource(schema);
        val providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(ds);
        providerFactory.setSelectClause(PRSN_Processor.SELECT_CLAUSE);
        providerFactory.setFromClause(PRSN_Processor.FROM_CLAUSE);
        providerFactory.setWhereClause("XIAREAID='"+xiareaid+"'");
        providerFactory.setSortKey("PK_R2575_NINO");
        try {
            return providerFactory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create PRSN_QueryProvider", e);
        }
    }

}
