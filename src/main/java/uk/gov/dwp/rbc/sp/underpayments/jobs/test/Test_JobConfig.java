package uk.gov.dwp.rbc.sp.underpayments.jobs.test;

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
//import uk.gov.dwp.rbc.sp.underpayments.jobs.load_pscs_data.load_ntedets.NTEDETS_StepConfig;
import uk.gov.dwp.rbc.sp.underpayments.jobs.load_relns.PRSNTOPRSN_StepConfig;


@Configuration
@EnableBatchProcessing
@Import({
        //NTEDETS_StepConfig.class,
        PRSNTOPRSN_StepConfig.class,
        JobConfigCommon.class})
@Slf4j
public class Test_JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    //---------------------------------------------------------------
    @Bean
    public Job testJob(
            Step load_PRSNTOPRSN_Step,
            //Step load_NTEDETS_Step,
            JobExecutionListener listener) {

        return jobBuilderFactory
                .get("test")
                .start(load_PRSNTOPRSN_Step)
                .listener(listener)
                .build();
    }

}
