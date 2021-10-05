package uk.gov.dwp.rbc.sp.underpayments.services.mi;

import lombok.val;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.util.ArrayList;
import java.util.List;

public class OverviewRec {

    public String name;
    private String filter;
    public long count;
    public List<OverviewRec> components = new ArrayList<>();
    private OverviewRec parent;

    //----------------------------------
    public static OverviewRec total(){

        val ov = new OverviewRec();
        ov.name = "Total Customers";
        ov.filter = "{}";
        return ov;
    }

    //-----------------------
    public OverviewRec with(String name, String filter){
        val ov = new OverviewRec();
        ov.name = name;
        ov.filter = filter;
        ov.parent = this;
        components.add(ov);
        return ov;
    }

    //-----------------------
    public OverviewRec endWith(){
        return parent;
    }

    //-----------------------
    public BasicQuery query(){
        try {
            return new BasicQuery(filter);
        }catch (Exception e){
            throw new UeException("Bad mongo query "+filter, e);
        }
    }

    @Override
    public String toString() {
        return "[name="+name+", filter="+filter+"]";
    }
}

