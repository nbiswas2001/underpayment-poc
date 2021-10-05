package uk.gov.dwp.rbc.sp.underpayments.jobs.load_awards;

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
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm.AW_AWCM_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;
import uk.gov.dwp.rbc.sp.underpayments.jobs.XiAreaIdParitioner;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.LOAD_AWARDS;

public class AWAWCM_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    PscsData pscsData;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MongoHelper mongoHelper;

    //---------------------------------------------------------------
    @Bean
    public Step load_AWAWCM_Step(Step load_AWAWCM_PStep,
                               XiAreaIdParitioner xiAreaIdParitioner,
                               TaskExecutor taskExecutor,
                               StepExecutionListener stepListener) {

        return stepBuilderFactory.get("load_AWAWCM")
                .partitioner(load_AWAWCM_PStep.getName(), xiAreaIdParitioner)
                .step(load_AWAWCM_PStep)
                .gridSize(appConfig.getBatchJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step load_AWAWCM_PStep(MongoItemReader<Account> loadAwardsReader,
                                MongoItemWriter<Account> accountWriter,
                                ChunkListener chunkListener,
                                AW_AWCM_Processor aw_awcm_processor) {

        return stepBuilderFactory.get("load_AWCM_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(loadAwardsReader)
                .processor(aw_awcm_processor)
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //-------------------------------------------
    @Bean
    @StepScope
    AW_AWCM_Processor aw_awcm_processor(@Value("#{stepExecutionContext['schema']}") String schema) {
        return new AW_AWCM_Processor(pscsData.getDataSource(schema));
    }

    //--------------------------------------------
    @Bean
    @StepScope
    MongoItemReader<Account> loadAwardsReader(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {

        return mongoHelper.getAccountReaderForStep(LOAD_AWARDS, schema, xiareaid);
    }
}
