package uk.gov.dwp.rbc.sp.underpayments.jobs.load_relns;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.JobConfigCommon;
import uk.gov.dwp.rbc.sp.underpayments.jobs.MongoHelper;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.LOAD_RELNS;

@Configuration
@EnableBatchProcessing
@Import({PRSNTOPRSN_StepConfig.class,
        JobConfigCommon.class})
@Slf4j
public class LoadRelns_JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    MongoHelper mongoHelper;


    //---------------------------------------------------------------
    @Bean
    public Job loadRelnsJob(
                               Step load_PRSNTOPRSN_Step,
                               JobExecutionListener jobListener) {

        return jobBuilderFactory
                .get("loadRelns")
                .start(load_PRSNTOPRSN_Step)
                .listener(jobListener)
                .build();
    }
}
