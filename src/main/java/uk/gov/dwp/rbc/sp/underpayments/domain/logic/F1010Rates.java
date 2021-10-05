package uk.gov.dwp.rbc.sp.underpayments.domain.logic;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.FileUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SacType.*;

@Component
@Slf4j
public class F1010Rates {

    private LocalDate ratesAvailableUpto;

    // BenefitCode -> Year (of tillDate) -> Entry
    Map<Integer, Map<Integer, F1010RateEntry>> rates = new HashMap<>();

    //--------------------------------------------------------
    public F1010RateEntry getEntry(SacType sacType, OffsetDateTime onDate){

        val sacCode = sacType.getF1010Code();
        if(onDate.isBefore(dt_15_11_1976)){
            throw new UeException("F1010 rates not available for "+onDate);
        }
        if(!rates.containsKey(sacCode)) throw new UeException("Invalid SAC type code "+sacCode);
        val codeMap = rates.get(sacCode);
        val y = onDate.getYear();
        var entry = getEntry(codeMap, y);
        if(onDate.isBefore(entry.startDate)) entry = entry.prevEntry;
        return entry;
    }

    private F1010RateEntry getEntry(Map<Integer, F1010RateEntry> codeMap, int year){
        if(!codeMap.containsKey(year)) throw new UeException("FR1010 Rate not available for year "+year);
        return codeMap.get(year);
    }

    //------------------------------------------------------------------------
    @PostConstruct
    public void init() {

        val relevantSacs = Set.of(
                CAT_A_BASIC.getF1010Code(),
                CAT_B_BASIC.getF1010Code(),
                CAT_BL_BASIC.getF1010Code(),
                CAT_D_BASIC.getF1010Code(),
                COMPOSITE_AB_BC.getF1010Code(),
                COMPOSITE_ABL_BC.getF1010Code(),
                IAA_HIGH.getF1010Code(),
                IAA_LOW.getF1010Code(),
                BC_INCS.getF1010Code()
        );

        F1010RateEntry[] prevEntryArr = { null };

        FileUtils.processResourceFile("F1010RATES.txt", line -> {
            val sacCode = Integer.parseInt(line.substring(0,3));
            if(!relevantSacs.contains(sacCode)) return; //These are not required

            Map<Integer, F1010RateEntry> codeMap = null;
            if(rates.containsKey(sacCode)) {
                codeMap = rates.get(sacCode);
            }
            else {
                codeMap = new HashMap<>();
                rates.put(sacCode, codeMap);
                prevEntryArr[0] = null;
            }

            val start_Y = Integer.parseInt(line.substring(3,7));

            if(start_Y == 9999) return; //last entry, dummy. lastDate of prevEntry is already null

            val start_M = Integer.parseInt(line.substring(7,9));
            val start_D = Integer.parseInt(line.substring(9,11));

            val entry = new F1010RateEntry();
            entry.startDate = DateUtils.dt(start_D, start_M, start_Y);
            entry.max = Integer.parseInt(line.substring(19,24));

            if(prevEntryArr[0] != null) {
                entry.prevEntry = prevEntryArr[0];
                entry.prevEntry.endDate = entry.startDate.minusDays(1);
            }
            prevEntryArr[0] = entry;

            codeMap.put(start_Y, entry);
        });
    }

    private static OffsetDateTime dt_15_11_1976 = DateUtils.dt(15,11,1976);
}
