package uk.gov.dwp.rbc.sp.underpayments.jobs.create_ac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.dwp.rbc.sp.underpayments.jobs.JobConfigCommon;


@Configuration
@EnableBatchProcessing
@Import({CreateAccount_StepConfig.class,
        JobConfigCommon.class})
@Slf4j
public class CreateAccount_JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    //---------------------------------------------------------------
    @Bean
    public Job createAccountJob(Step createAccount_Step,
                               JobExecutionListener jobListener) {

        return jobBuilderFactory
                .get("createAccount")
                .start(createAccount_Step)
                .listener(jobListener)
                .build();
    }



}
