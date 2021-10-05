package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StopWatch;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig.AccountPartitionGenerator;

import javax.sql.DataSource;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep.*;

@Configuration
@Slf4j
public class JobConfigCommon {

    @Autowired
    PscsData pscsData;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MongoHelper mongoHelper;

    @Autowired
    AccountPartitionGenerator accountPartitionGenerator;


    //--------------------------------------------
    @Bean
    public XiAreaIdParitioner xiAreaIdPartitioner() {
        return new XiAreaIdParitioner(pscsData);
    }

    //--------------------------------------------
    @Bean
    public AccountPartitioner acPartitionPartitioner() {
        return new AccountPartitioner(appConfig.getBatchJobThreads(), pscsData);
    }

    //---------------------------------
    @Bean
    public TaskExecutor taskExecutor() {

        val maxThreads = appConfig.getBatchJobThreads();
        val minThreads = appConfig.getCalcJobThreads();

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(maxThreads);
        taskExecutor.setCorePoolSize(minThreads);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    //--------------------------------------------
    @Bean
    public MongoItemWriter<Account> accountWriter(MongoTemplate mongoTemplate) {
        return new MongoItemWriterBuilder<Account>()
                .template(mongoTemplate).collection("account")
                .build();
    }

    //--------------------------------------
    @Bean
    @JobScope
    public JobExecutionListener jobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                val jobName = jobExecution.getJobInstance().getJobName();
                log.info("JOB STARTING - "+jobName);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("JOB COMPLETED");
                }
                if(jobExecution.getJobInstance().getJobName().equals("calcCircsEligibility")){
                    mongoHelper.ensureIndicesForReports();
                }
            }
        };
    }

    //----------------------------------
    @Bean
    @StepScope
    public StepExecutionListener stepListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                val stepName = stepExecution.getStepName();
                CalcStep step = null;
                switch (stepName){
                    case "createAccount": step = CREATE_ACCOUNT; break;
                    case "calcAccountEligibility": step = CALC_AC_ELIGIBILITY; break;
                    case "load_PRSNTOPRSN": step = LOAD_RELNS; break;
                    case "load_AWCM": step = LOAD_AWARDS; break;
                    case "calcCircsEligibility": step = CALC_CIRCS_ELIGIBILITY; break;
                    case "calcEntitlement": step = CALC_ENTITLEMENT; break;
                }
                mongoHelper.ensureIndicesForStep(step);

                if(step.equals(CALC_AC_ELIGIBILITY)){
                    accountPartitionGenerator.init();
                }
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return ExitStatus.COMPLETED;
            }
        };
    }

    //----------------------------------
    @Bean
    @StepScope
    public ChunkListener chunkListener() {

        return new ChunkListener() {
            private String name = "";
            private int counter = 0;

            @Override
            public void beforeChunk(ChunkContext chunkContext) {
                val sc = chunkContext.getStepContext();

                val n1 = sc.getStepName();
                if(n1.equals(name)){
                    name = n1;
                    counter = 1;
                }
                else counter ++;

                val sb2 = new StringBuilder();
                sb2.append(n1).append(".Chunk ").append(counter);
                val n2 = sb2.toString();

                val sw = new StopWatch(n2);
               sw.start();
               chunkContext.setAttribute("n", n2);
               chunkContext.setAttribute("w", sw);
               log.info("Starting chunk");
            }

            @Override
            public void afterChunk(ChunkContext chunkContext) {
                whenDone(chunkContext);
            }

            @Override
            public void afterChunkError(ChunkContext chunkContext) {
                whenDone(chunkContext);
            }

            private void whenDone(ChunkContext chunkContext){
                val n = (String) chunkContext.getAttribute("n");
                val sw = (StopWatch) chunkContext.getAttribute("w");
                sw.stop();
                val sb = new StringBuilder();
                sb.append(n).append(": ").append(sw.getTotalTimeMillis()).append(" ms");
                log.info(sb.toString());
                chunkContext.removeAttribute("n");
                chunkContext.removeAttribute("w");
            }
        };
    }
}
