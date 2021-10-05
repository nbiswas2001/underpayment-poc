package uk.gov.dwp.rbc.sp.underpayments.jobs.load_relns;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn.PRSNTOPRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.AccountPartitioner;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;
import uk.gov.dwp.rbc.sp.underpayments.jobs.XiAreaIdParitioner;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.LOAD_RELNS;

public class PRSNTOPRSN_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    PscsData pscsData;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    AppConfig appConfig;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    MongoHelper mongoHelper;

    //---------------------------------------------------------------
    @Bean
    public Step load_PRSNTOPRSN_Step(Step load_PRSNTOPRSN_PStep,
                                     XiAreaIdParitioner xiAreaIdParitioner,
                                     TaskExecutor taskExecutor,
                                     StepExecutionListener stepListener) {

        return stepBuilderFactory.get("load_PRSNTOPRSN")
                .partitioner(load_PRSNTOPRSN_PStep.getName(), xiAreaIdParitioner)
                .step(load_PRSNTOPRSN_PStep)
                .gridSize(appConfig.getBatchJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step load_PRSNTOPRSN_PStep(MongoItemReader<Account> loadRelnsReader,
                                      MongoItemWriter<Account> accountWriter,
                                      ChunkListener chunkListener,
                                      PRSNTOPRSN_Processor prsntoprsn_processor) {

        return stepBuilderFactory.get("load_PRSNTOPRSN_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(loadRelnsReader)
                .processor(prsntoprsn_processor)
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //-------------------------------------------
    @Bean
    @StepScope
    PRSNTOPRSN_Processor prsntoprsn_processor(@Value("#{stepExecutionContext['schema']}") String schema) {
        return new PRSNTOPRSN_Processor(cryptoUtils, pscsData.getDataSource(schema), accountRepo);
    }

    //--------------------------------------------
    @Bean
    @StepScope
    MongoItemReader<Account> loadRelnsReader(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {

        return mongoHelper.getAccountReaderForStep(LOAD_RELNS, schema, xiareaid);
    }
}
