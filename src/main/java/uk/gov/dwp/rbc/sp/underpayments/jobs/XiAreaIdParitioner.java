package uk.gov.dwp.rbc.sp.underpayments.jobs;

import lombok.val;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;

import java.util.HashMap;
import java.util.Map;

public class XiAreaIdParitioner implements Partitioner {

    private PscsData pscsData;
    Map<String, ExecutionContext> result;

    //------------------------------------------
    public XiAreaIdParitioner(PscsData pscsData){
        this.pscsData = pscsData;
    }

    //--------------------------------------------------------
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {


        if(result == null) {
            result = new HashMap<>();
            for(val schema : pscsData.getSchemas()){
                val ds = pscsData.getDataSource(schema);
                val jdbc = new JdbcTemplate(ds);
                val xiareaids = jdbc.query("select distinct XIAREAID from R2575PRSN order by XIAREAID",
                        (resultSet, i1) -> resultSet.getNString(1));


                int number = 1;
                int total = xiareaids.size();
                for (val xiareaid : xiareaids) {
                    val ec = new ExecutionContext();
                    ec.putString("schema", schema);
                    ec.putString("XIAREAID", xiareaid);
                    result.put("Partition " + number + "/" + total+" ["+xiareaid+"]", ec);
                    number++;
                }
            }
        }

        return result;
    }
}
