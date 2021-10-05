package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_circs_elig;

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
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.CircsLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.AccountPartitioner;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_CIRCS_ELIGIBILITY;

public class CalcCircsEligibility_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    CircsLogic circsLogic;

    @Autowired
    EligibilityLogic eligibilityLogic;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MongoHelper mongoHelper;

    //---------------------------------------------------------------
    @Bean
    public Step calcCircsEligibility_Step(Step calcCircsEligibility_PStep,
                                          AccountPartitioner accountPartitioner,
                                          TaskExecutor taskExecutor,
                                          StepExecutionListener stepListener) {
        return stepBuilderFactory.get("calcCircsEligibility")
                .partitioner(calcCircsEligibility_PStep.getName(), accountPartitioner)
                .step(calcCircsEligibility_PStep)
                .gridSize(appConfig.getCalcJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    //---------------------------------------------------------------
    @Bean
    public Step calcCircsEligibility_PStep(MongoItemReader<Account> calcCircsEligibilityReader,
                                           MongoItemWriter<Account> accountWriter,
                                           ChunkListener chunkListener) {
        return stepBuilderFactory.get("calcCircsEligibility_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(calcCircsEligibilityReader)
                .processor(new CalcCircsEligibility_Processor(circsLogic, eligibilityLogic))
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //--------------------------------------------
    @Bean
    @StepScope
    MongoItemReader<Account> calcCircsEligibilityReader(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['partition']}") String partition) {

        return mongoHelper.getAccountReaderForStep(CALC_CIRCS_ELIGIBILITY, schema, partition);
    }

}
