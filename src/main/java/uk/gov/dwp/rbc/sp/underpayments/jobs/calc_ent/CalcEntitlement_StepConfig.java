package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ent;

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
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EntitlementLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.AccountPartitioner;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_ENTITLEMENT;

public class CalcEntitlement_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    EntitlementLogic entitlementLogic;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MongoHelper mongoHelper;

    //---------------------------------------------------------------
    @Bean
    public Step calcEntitlement_Step(Step calcEntitlement_PStep,
                                     AccountPartitioner accountPartitioner,
                                     TaskExecutor taskExecutor,
                                     StepExecutionListener stepListener) {
        return stepBuilderFactory.get("calcEntitlement")
                .partitioner(calcEntitlement_PStep.getName(), accountPartitioner)
                .step(calcEntitlement_PStep)
                .gridSize(appConfig.getCalcJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step calcEntitlement_PStep(MongoItemReader<Account> calcEntitlementReader,
                                      MongoItemWriter<Account> accountWriter,
                                      ChunkListener chunkListener) {
        return stepBuilderFactory.get("calcEntitlement_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(calcEntitlementReader)
                .processor(new CalcEntitlement_Processor(entitlementLogic))
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //--------------------------------------------
    @Bean
    @StepScope
    MongoItemReader<Account> calcEntitlementReader(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['partition']}") String partition) {

        return mongoHelper.getAccountReaderForStep(CALC_ENTITLEMENT, schema, partition);
    }
}
