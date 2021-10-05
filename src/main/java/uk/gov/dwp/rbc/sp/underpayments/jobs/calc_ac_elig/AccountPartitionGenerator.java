package uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class AccountPartitionGenerator {

    @Autowired
    AppConfig appConfig;

    private AtomicLong counter;
    private int numPartitions;

    public int getPartition() {
        return Math.toIntExact(counter.incrementAndGet() % numPartitions) + 1;
    }

    public void init() {
        numPartitions = appConfig.getBatchJobThreads();
        counter = new AtomicLong(0);
    }

}
