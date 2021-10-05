package uk.gov.dwp.rbc.sp.underpayments.services.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.util.List;

@Component
@Slf4j
public class JobService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private ApplicationContext context;

    private static List<String> jobNames = List.of(
            "createAccount",
            "calcAccountEligibility",
            "loadRelns",
            "loadAwards",
            "calcCircsEligibility",
            "calcEntitlement"
    );

    //----------------------------------------------------
    public void runJob(String jobName) {

        if(!jobNames.contains(jobName)){
            throw new UeException("Unknown Job "+jobName);
        }

        log.info("Starting Job "+jobName);
        Job job = context.getBean(jobName+"Job", Job.class);
        BatchStatus status = BatchStatus.UNKNOWN;
        try {
            // To enable multiple execution of a job with the same parameters
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            final JobExecution execution = jobLauncher.run(job, jobParameters);
            status = execution.getStatus();
        } catch (final Exception e) {
            throw new UeException("Job "+jobName+" failed", e);
        }

        if(status.equals(BatchStatus.FAILED)){
            throw new UeException("Job "+jobName+" failed");
        }

        log.info("Job succeeded");
    }
}
