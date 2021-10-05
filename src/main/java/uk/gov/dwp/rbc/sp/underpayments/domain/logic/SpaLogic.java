package uk.gov.dwp.rbc.sp.underpayments.domain.logic;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Citizen;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Sex;
import uk.gov.dwp.rbc.sp.underpayments.utils.DateUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.Account.AgeCategory.*;


@Component
public class SpaLogic {


    //----------------------------------------------------------------------------
    public Account.AgeCategory getAgeCategory(Account account, OffsetDateTime calcDate){

        //Take the reference date as either the calc date or, if deceased, the date of death
        val refDate = account.getDateOfDeath() !=null ? account.getDateOfDeath(): calcDate;

        val dob = account.getDateOfBirth();
        val sex = account.getSex();
        val ageY = ChronoUnit.YEARS.between(dob, refDate);

        if(ageY >= 80) return OVER_80;
        else if(getDateOfSpa(sex, dob).isAfter(refDate)){
            return UNDER_SPA;
        }
        else return SPA;
    }

    //---------------------------------------------------------------------------------
    /*
    See https://en.wikipedia.org/wiki/State_Pension_(United_Kingdom)
    and https://www.gov.uk/government/publications/state-pension-age-timetable/state-pension-age-timetable
    */
    public OffsetDateTime getDateOfSpa(Sex sex, OffsetDateTime dob) {

        //For women born before 6/4/1950
        if (sex.equals(Sex.F) && dob.isBefore(dt_6_4_1950)) {
            return dob.plusYears(60); //SPA date is 60th birthday
        }
        //For men born before 6/12/1953
        else if (sex.equals(Sex.M) && dob.isBefore(dt_6_12_1953)) {
            return dob.plusYears(65); //SPA date is 65 birthday
        }
        //For women born between 6/4/1950 and 5/12/1953 inc
        else if (sex.equals(Sex.F) && DateUtils.isDateBetween(dob, dt_6_4_1950, dt_5_12_1953)) {

            val idx = getMapKeys(dob);
            return spaDate_F_1950to53.get(idx.getLeft()).get(idx.getRight()); //SPA date is given by Table 1
        }
        //For men and women born between 6/12/1953 and 5/10/1954 inc
        else if(DateUtils.isDateBetween(dob, dt_6_12_1953, dt_5_10_1954)) {
            val idx = getMapKeys(dob);
            return spaDate_1953to54.get(idx.getLeft()).get(idx.getRight()); //SPA date is given by Table 2
        }
        //For men and women born between 6/10/1954 and 5/4/1960
        else if(DateUtils.isDateBetween(dob, dt_6_10_1954, dt_5_4_1960)) {
            return dob.plusYears(66); //SPA date is 66th birthday
        }
        //For men and women born between 6/4/1960 and 5/3/1961 inc
        else if(DateUtils.isDateBetween(dob, dt_6_4_1960, dt_5_3_1961)) {
            //SPA data is given by date at 66yrs and N months, where N is given by Table 3
            val idx = getMapKeys(dob);
            val monthsToAdd = spa_1960to61.get(idx.getLeft()).get(idx.getRight());
            return dob.plusYears(66).plusMonths(monthsToAdd);
        }
        //For men and women born between 6/3/1961 and 5/4/1977 inc
        else if(DateUtils.isDateBetween(dob, dt_6_3_1961, dt_5_4_1977)) {
            return dob.plusYears(67); //SPA date is 67th birthday
        }
        //For men and women born after 5/4/1977, assume SPA is 68 (doesn't matter)
        else {
            return dob.plusYears(68);
        }
    }

    //---------------------------------------------------------------------------------
    private static final OffsetDateTime dt_6_4_1950 = dt(6,4,1950);
    private static final OffsetDateTime dt_5_12_1953 = dt(5,12,1953);
    private static final OffsetDateTime dt_6_12_1953 = dt(6,12,1953);
    private static final OffsetDateTime dt_5_10_1954 = dt(5,10,1954);
    private static final OffsetDateTime dt_6_10_1954 = dt(6,10,1954);
    private static final OffsetDateTime dt_5_4_1960 = dt(5,4,1960);
    private static final OffsetDateTime dt_6_4_1960 = dt(6,4,1960);
    private static final OffsetDateTime dt_5_3_1961 = dt(5,3,1961);
    private static final OffsetDateTime dt_6_3_1961 = dt(6,3,1961);
    private static final OffsetDateTime dt_5_4_1977 = dt(5,4,1977);

    private static OffsetDateTime dt(int d, int m, int y){
        return OffsetDateTime.of(y, m, d,0,0,0, 0, ZoneOffset.UTC);
    }
    //----------------------------------------------------------------------------------
    // Table 1
    private static final Map<Integer, Map<Integer, OffsetDateTime>> spaDate_F_1950to53 = Map.of(
            1950, Map.ofEntries(
                    Map.entry(4, dt(6,  5, 2010)),
                    Map.entry(5, dt(6,  7, 2010)),
                    Map.entry(6, dt(6,  9, 2010)),
                    Map.entry(7, dt(6,  11, 2010)),
                    Map.entry(8, dt(6,  1, 2011)),
                    Map.entry(9, dt(6,  3, 2011)),
                    Map.entry(10, dt(6,  5, 2011)),
                    Map.entry(11, dt(6,  7, 2011)),
                    Map.entry(12, dt(6,  9, 2011))
            ),
            1951, Map.ofEntries(
                    Map.entry(1, dt(6,  11, 2011)),
                    Map.entry(2, dt(6,  1, 2012)),
                    Map.entry(3, dt(6,  3, 2012)),
                    Map.entry(4, dt(6,  5, 2012)),
                    Map.entry(5, dt(6,  7, 2012)),
                    Map.entry(6, dt(6,  9, 2012)),
                    Map.entry(7, dt(6,  11, 2012)),
                    Map.entry(8, dt(6,  1, 2013)),
                    Map.entry(9, dt(6,  3, 2013)),
                    Map.entry(10, dt(6,  5, 2013)),
                    Map.entry(11, dt(6,  7, 2013)),
                    Map.entry(12, dt(6,  9, 2013))
            ),
            1952, Map.ofEntries(
                    Map.entry(1, dt(6,  11, 2013)),
                    Map.entry(2, dt(6,  1, 2014)),
                    Map.entry(3, dt(6,  3, 2014)),
                    Map.entry(4, dt(6,  5, 2014)),
                    Map.entry(5, dt(6,  7, 2014)),
                    Map.entry(6, dt(6,  9, 2014)),
                    Map.entry(7, dt(6,  11, 2014)),
                    Map.entry(8, dt(6,  1, 2015)),
                    Map.entry(9, dt(6,  3, 2015)),
                    Map.entry(10, dt(6,  5, 2015)),
                    Map.entry(11, dt(6,  7, 2015)),
                    Map.entry(12, dt(6,  9, 2015))
            ),
            1953, Map.ofEntries(
                    Map.entry(1, dt(6,  11, 2015)),
                    Map.entry(2, dt(6,  1, 2016)),
                    Map.entry(3, dt(6,  3, 2016)),
                    Map.entry(4, dt(6,  7, 2016)),
                    Map.entry(5, dt(6,  11, 2016)),
                    Map.entry(6, dt(6,  3, 2017)),
                    Map.entry(7, dt(6,  7, 2017)),
                    Map.entry(8, dt(6,  11, 2017)),
                    Map.entry(9, dt(6,  3, 2018)),
                    Map.entry(10, dt(6,  7, 2018)),
                    Map.entry(11, dt(6,  11, 2018))
            )
    );

    //---------------------------------------------------------------------------------
    // Table 2
    private static final Map<Integer, Map<Integer, OffsetDateTime>> spaDate_1953to54 = Map.of(
            1953, Map.ofEntries(
                    Map.entry(12, dt(6,3,2019))
            ),
            1954, Map.ofEntries(
                    Map.entry(1, dt(6,5,2019)),
                    Map.entry(2, dt(6,7,2019)),
                    Map.entry(3, dt(6,9,2019)),
                    Map.entry(4, dt(6,11,2019)),
                    Map.entry(5, dt(6,1,2020)),
                    Map.entry(6, dt(6,3,2020)),
                    Map.entry(7, dt(6,5,2020)),
                    Map.entry(8, dt(6,7,2020)),
                    Map.entry(9, dt(6,9,2020))
            )
    );
    //---------------------------------------------------------------------------------
    // Table 3
    private static final Map<Integer, Map<Integer, Integer>> spa_1960to61 = Map.of(
            1960, Map.ofEntries(
                    Map.entry(4, 1),
                    Map.entry(5, 2),
                    Map.entry(6, 3),
                    Map.entry(7, 4),
                    Map.entry(8, 5),
                    Map.entry(9, 6),
                    Map.entry(10, 7),
                    Map.entry(11, 8),
                    Map.entry(12, 9)
            ),
            1961, Map.ofEntries(
                    Map.entry(1, 10),
                    Map.entry(2, 11)
            )
    );

    //----------------------------------------------------------------
    //Map keys calculated from DoB for Tables 1 and 2
    private static Pair<Integer, Integer> getMapKeys(OffsetDateTime dob) {
        var y = dob.getYear();
        var m = dob.getMonthValue();
        if(dob.getDayOfMonth() < 6) m-- ;
        if(m==0) {
            y--;
            m = 12;
        }
        return Pair.of(y, m);
    }

}
