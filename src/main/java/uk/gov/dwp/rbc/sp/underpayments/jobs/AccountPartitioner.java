package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.val;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;

import java.util.HashMap;
import java.util.Map;

public class AccountPartitioner implements Partitioner {

    Map<String, ExecutionContext> result;

    private int numPartitions;

    private PscsData pscsData;


    public AccountPartitioner(int numPartitions, PscsData pscsData) {
        this.numPartitions = numPartitions;
        this.pscsData = pscsData;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        if(result == null) {
            result = new HashMap<>();
            for(val schema : pscsData.getSchemas()) {
                for (int i = 1; i <= numPartitions; i++) {
                    val ec = new ExecutionContext();
                    ec.putString("schema", schema);
                    ec.putString("partition", Integer.toString(i));
                    result.put("Partition " + i + "/" + numPartitions+" ["+schema+"]", ec);
                }
            }
        }

        return result;
    }
}
