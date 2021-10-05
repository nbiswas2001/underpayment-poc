package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig;

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
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;
import uk.gov.dwp.rbc.sp.underpayments.jobs.XiAreaIdParitioner;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.CALC_AC_ELIGIBILITY;

public class CalcAccountEligibility_StepConfig {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    EligibilityLogic eligibilityLogic;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MongoHelper mongoHelper;

    //---------------------------------------------------------------
    @Bean
    public Step calcAccountEligibility_Step(Step calcAccountEligibility_PStep,
                                            XiAreaIdParitioner xiAreaIdPartitioner,
                                            TaskExecutor taskExecutor,
                                            StepExecutionListener stepListener) {

        return stepBuilderFactory.get("calcAccountEligibility")
                .partitioner(calcAccountEligibility_PStep.getName(), xiAreaIdPartitioner)
                .step(calcAccountEligibility_PStep)
                .gridSize(appConfig.getCalcJobThreads())
                .taskExecutor(taskExecutor)
                .listener(stepListener)
                .build();
    }

    static int count = 0;
    //---------------------------------------------------------------
    @Bean
    public Step calcAccountEligibility_PStep(MongoItemReader<Account> calcAccountEligibilityReader,
                                             MongoItemWriter<Account> accountWriter,
                                             ChunkListener chunkListener) {

        return stepBuilderFactory.get("calcAccountEligibility_P")
                .<Account, Account>chunk(appConfig.getBatchJobChunkSize())
                .reader(calcAccountEligibilityReader)
                .processor(new CalcAccountEligibility_Processor(eligibilityLogic))
                .writer(accountWriter)
                .listener(chunkListener)
                .build();
    }

    //--------------------------------------------
    @Bean
    @StepScope
    MongoItemReader<Account> calcAccountEligibilityReader(
            @Value("#{stepExecutionContext['schema']}") String schema,
            @Value("#{stepExecutionContext['XIAREAID']}") String xiareaid) {

        return mongoHelper.getAccountReaderForStep(CALC_AC_ELIGIBILITY, schema, xiareaid);
    }
}
