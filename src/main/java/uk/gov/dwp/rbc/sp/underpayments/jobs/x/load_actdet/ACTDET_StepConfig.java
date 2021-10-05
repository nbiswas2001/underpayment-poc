package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_actdet;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.XiAreaIdParitioner;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

import javax.sql.DataSource;

public class ACTDET_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("pscsDatasource")
    DataSource pscsDataSource;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    AppConfig appConfig;

    //---------------------------------------------------------------
    @Bean
    public Step load_ACTDET_Step(Step load_ACTDET_PStep,
                                XiAreaIdParitioner partitioner,
                                TaskExecutor taskExecutor) {

        return stepBuilderFactory.get("load_ACTDET")
                .partitioner(load_ACTDET_PStep.getName(), partitioner)
                .step(load_ACTDET_PStep)
                .gridSize(appConfig.getBatchJobThreads())
                .taskExecutor(taskExecutor)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step load_ACTDET_PStep(JdbcPagingItemReader<R2501ACT_DET> ACTDET_Reader,
                                    MongoItemWriter<Account> accountWriter) {
        return stepBuilderFactory.get("load_ACTDET_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                //.reader(ACTDET_Reader)
                .processor(new ACTDET_Processor())
                .writer(accountWriter)
                .build();
    }

    //---------------------------------
    @Bean
    public JdbcPagingItemReader<R2501ACT_DET> ACTDET_Reader(PagingQueryProvider ACTDET_QueryProvider) {

        return new JdbcPagingItemReaderBuilder<R2501ACT_DET>()
                .name("ACTDET_Reader")
                .dataSource(pscsDataSource)
                .queryProvider(ACTDET_QueryProvider)
                .rowMapper(new ACTDET_RowMapper())
                .pageSize(appConfig.getBatchJobChunkSize())
                .build();
    }

    //---------------------------------------------
    @Bean
    @StepScope
    public PagingQueryProvider ACTDET_QueryProvider(
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {
        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(pscsDataSource);
        providerFactory.setSelectClause("PKF_R2577_NINO, max(D2501_IOP_END_DT) as MAX_D2501_IOP_END_DT");
        providerFactory.setFromClause("R2501ACT_DET");
        providerFactory.setGroupClause("PKF_R2577_NINO");
        providerFactory.setWhereClause("D2501_IOP_STAT_TP = 1 and XIAREAID = '"+xiareaid+"'");
        providerFactory.setSortKey("PKF_R2577_NINO");
        try {
            return providerFactory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create ACTDET_QueryProvider", e);
        }
    }

}
