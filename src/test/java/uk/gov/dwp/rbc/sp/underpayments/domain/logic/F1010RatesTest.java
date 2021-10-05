package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class F1010RatesTest {

    @Autowired
    F1010Rates f1010Rates;

    @Test
    void getEntry() {
        var rt = f1010Rates.getEntry(SacType.CAT_A_BASIC, dt(10,4,2019));
        assertEquals(12920, rt.max);
        rt = f1010Rates.getEntry(SacType.CAT_D_BASIC, dt(10,4,2019));
        assertEquals(7745, rt.max);
        rt = f1010Rates.getEntry(SacType.CAT_A_BASIC, dt(7,6,2020));
        assertEquals(13425, rt.max);
        rt = f1010Rates.getEntry(SacType.CAT_D_BASIC, dt(7,6,2020));
        assertEquals(8045, rt.max);

    }

    @Test
    void debug() {

        for(val sacType : f1010Rates.rates.keySet()){
            val rtMap = f1010Rates.rates.get(sacType);
            for(val x : rtMap.keySet()){
                if(x == 2021){
                    System.out.println(sacType+":"+rtMap.get(x).startDate+":"+rtMap.get(x).endDate);
                }
            }
        }
        //val rt = f1010Rates.getEntry(SacType.CAT_BL_BASIC, LocalDate.of(2021, 8, 7));
    }

    private OffsetDateTime dt(int d, int m, int y){
        return DateUtils.dt(d,m,y);
    }
}