package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_ntedets;

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

public class NTEDETS_StepConfig {

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

    //-------------------------------------------------------------------------------
    @Bean
    public Step load_NTEDETS_Step(Step load_NTEDETS_PStep,
                                  XiAreaIdParitioner partitioner,
                                  TaskExecutor taskExecutor) {

        return stepBuilderFactory.get("load_NTEDETS")
                .partitioner(load_NTEDETS_PStep.getName(), partitioner)
                .step(load_NTEDETS_PStep)
                .gridSize(appConfig.getBatchJobThreads())
                .taskExecutor(taskExecutor)
                .build();
    }
    //-------------------------------------------------------------------------------
    @Bean
    public Step load_NTEDETS_PStep(JdbcPagingItemReader<R2816NTEDETS> NTEDETS_Reader,
                                   MongoItemWriter<Account> accountWriter) {

        return stepBuilderFactory.get("load_NTEDETS_P")
                .<R2816NTEDETS, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(NTEDETS_Reader)
                .processor(new NTEDETS_Processor(accountRepo, cryptoUtils))
                .writer(accountWriter)
                .build();
    }

    //----------------------------------------------------------------------------------------
    @Bean
    @StepScope
    public JdbcPagingItemReader<R2816NTEDETS> NTEDETS_Reader(PagingQueryProvider NTEDETS_QueryProvider) {

        return new JdbcPagingItemReaderBuilder<R2816NTEDETS>()
                .name("NTEDETS_Reader")
                .dataSource(pscsDataSource)
                .queryProvider(NTEDETS_QueryProvider)
                .rowMapper(new NTEDETS_RowMapper())
                .pageSize(appConfig.getBatchJobChunkSize())
                .build();
    }

    //---------------------------------------------
    @Bean
    @StepScope
    public PagingQueryProvider NTEDETS_QueryProvider(
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {
        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(pscsDataSource);
        providerFactory.setSelectClause("PK_R2816_NINO,D2816_NTE_TX_LNE_01,D2816_NTE_TX_LNE_02,D2816_NTE_TX_LNE_03,"+
                "D2816_NTE_TX_LNE_04,D2816_NTE_TX_LNE_05,D2816_NTE_TX_LNE_06,D2816_NTE_TX_LNE_07,D2816_NTE_TX_LNE_08,"+
                "D2816_NTE_TX_LNE_09,D2816_NTE_TX_LNE_10,D2816_NTE_TX_LNE_11,D2816_NTE_TX_LNE_12,D2816_NTE_TX_LNE_13,"+
                "D2816_NTE_TX_LNE_14,D2816_NTE_TX_LNE_15,D2816_NTE_TX_LNE_16,D2816_NTE_TX_LNE_17");
        providerFactory.setFromClause("R2816NTEDETS");
        providerFactory.setWhereClause("XIAREAID='"+xiareaid+"'");
        providerFactory.setSortKey("PK_R2816_NINO");
        try {
            return providerFactory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create NTEDETS_QueryProvider", e);
        }
    }

}
