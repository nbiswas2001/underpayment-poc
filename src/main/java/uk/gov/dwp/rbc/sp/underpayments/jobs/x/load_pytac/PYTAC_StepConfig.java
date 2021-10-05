package uk.gov.dwp.rbc.sp.underpayments.jobs.x.load_pytac;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

import javax.sql.DataSource;

public class PYTAC_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    DataSource pscsDataSource;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    AppConfig appConfig;

    //---------------------------------------------------------------
    @Bean
    public Step load_PYTAC_Step(JdbcPagingItemReader<R2577PYTAC> PYTAC_Reader,
                                MongoItemWriter<Account> accountWriter) {
        return stepBuilderFactory.get("load_PRSN")
                .<R2577PYTAC, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(PYTAC_Reader)
                .processor(new PYTAC_Processor(accountRepo, cryptoUtils))
                .writer(accountWriter)
                .build();
    }

    //---------------------------------
    @Bean
    public JdbcPagingItemReader<R2577PYTAC> PYTAC_Reader(PagingQueryProvider PYTAC_QueryProvider) {

        return new JdbcPagingItemReaderBuilder<R2577PYTAC>()
                .name("PYTAC_Reader")
                .dataSource(pscsDataSource)
                .queryProvider(PYTAC_QueryProvider)
                .rowMapper(new PYTAC_RowMapper())
                .pageSize(appConfig.getBatchJobChunkSize())
                .build();
    }

    //---------------------------------------------
    @Bean
    public PagingQueryProvider PYTAC_QueryProvider() {
        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(pscsDataSource);
        providerFactory.setSelectClause("PK_R2577_NINO,D2577_IOP_TP,D2577_MOP_INSTR_STRT_DT,D2577_MOP_INSTR_END_DT,D2577_PRD_TP,D2577_PYT_SUSP_FG,D2577_PYTAC_D01,D2577_PYTAC_D02,D2577_PYTAC_D03,D2577_PYTAC_D04,D2577_PYTAC_D05,D2577_PYTAC_D06,D2577_PYTAC_D07,D2577_PYTAC_D08,D2577_PYTAC_D09,D2577_PYTAC_D10,D2577_PYTAC_D11,D2577_PYTAC_D12,D2577_PYTAC_D13,D2577_PYTAC_D14,D2577_PYTAC_D15,D2577_PYTAC_D16,D2577_PYTAC_D17,D2577_PYTAC_D18,D2577_PYTAC_D19,D2577_PYTAC_D20,D2577_PYTAC_D21,D2577_PYTAC_D22,D2577_PYTAC_D23,D2577_PYTAC_D24,D2577_PYTAC_D25,D2577_PYTAC_D26,D2577_PYTAC_D27");
        providerFactory.setFromClause("R2577PYTAC");
        providerFactory.setSortKey("PK_R2577_NINO");
        try {
            return providerFactory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create PAYTAC_QueryProvider", e);
        }
    }

}
