package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_circs_elig;

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
@Import({
        CalcCircsEligibility_StepConfig.class,
        JobConfigCommon.class})
@Slf4j
public class CalcCircsEligibility_JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;


    //---------------------------------------------------------------
    @Bean
    public Job calcCircsEligibilityJob(
            Step calcCircsEligibility_Step,
            JobExecutionListener jobListener) {

        return jobBuilderFactory
                .get("calcCircsEligibility")
                .start(calcCircsEligibility_Step)
                .listener(jobListener)
                .build();
    }

}
