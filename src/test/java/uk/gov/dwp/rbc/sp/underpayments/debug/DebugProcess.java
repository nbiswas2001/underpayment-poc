package uk.gov.dwp.rbc.sp.underpayments.debug;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig.AccountPartitionGenerator;
import uk.gov.dwp.rbc.sp.underpayments.services.narrator.NUtils;

import java.io.FileWriter;
import java.io.IOException;

import static uk.gov.dwp.rbc.sp.underpayments.debug.DebugLoader.Stage.*;

@SpringBootTest
public class DebugProcess {

    @Autowired
    AccountPartitionGenerator partitionGenerator;

    //PSL1 *******************
    //AS802599 - Deceased, but why no entitlement? No pay day or pmt in advance?
    //AC136477 - award changes within a circ. We should look at award 3 as well.
    //BB473621 - has spurious awards - SOLVED
    //AC136477 - Cat D added amount missing - SOLVED
    //EG185199 - Not breaking out of while loop - SOLVED
    //BG420640, BW800230 - Rate is null. Entitlement start date is before the earliest F1010 date.CLAIM is after REACHED_80.
    //PP507681 - Getting unverified DoD warning when DoD is empty
    //XN899782 - FR1010 Rate not available for year 2026 - SOLVED (conversion cases, marked as TOO_COMPLEX)
    //PH382881, PR888114 - Eligible for BL in mongo, but Ineligible in narrator - SOLVED (Sort was wrong in AcDataReader for AWCM)
    //BA733043 - Already has full Cat BL but missed (picking up wrong awcm). Also why is spouseSPADate missing.
    //           Why is ben start date before ent start date. SOLVED - Now treating entitlementStart date as start date of SP.
    //PG613830 - All good but why is 'needs to claim' false. Note claims can be backdated only to 12 months - SOLVED
    //AC837165 - Person has full Cat B (2). Check for AB (11). So set max award. - SOLVED added maxAward() check
    //PJ111253 - UeException: Cannot compare dates as both are null (DateUtils.java:30). Getting 2 PRLBNs, one RP and one RP-WB
    //PP839743,MK740137 - FR1010 Rate not available for year 2022 - SOLVED. Circ REACHED_80 was getting created after calc date
    //XT664801 - Null pointer exception @EligibilityLogic.java:151
    //PT522691 - Both RP and RP-WB prlbns - SOLVED, only loading 'RP'
    //CP527723 - Diff results in batch and narr. Dates are loaded wrong in batch, -1 day. Awards not loaded.
    //PH849840 - Entitlement start date is way after benefit start date. Maybe we should use benefit start date as spStartDate?
    //PZ537915 - Only need to add the balance Cat BL (2%), not 100%
    //JC931236 - Rates were frozen, hence Int'l (R2509AWCMPRD -> D2509_BEN_CM_UPRTG_DT should have a value > 0)
    //PH695243 - 'Needs to claim' rules are wrong. Is true here, but should be false
    //XG944958 - Pro Rata fraction (D2508_PRFR_D_CNT, D2508_PRFR_N_CNT) present on AWCM, hence Int'l. BUT THIS IS INCORRECT as it is = 0 in this case
    //AN063631 - The actual award starts 6 days after the beginning of the REACHED_80 circ. Based on arrears/advance look for awards starting
    //              either within previous/next 7 days. Also example of superceded valid awcms
    //BM219765 - Should consider BC INCS sac from Mark Bowerman's list (don't know sac code)
    //OZ060068 - Check this one. Cat BL is awarded 1 year later. So need to load all awards within a circ to get correct entitlement
    //PSK1 ***********************
    //PH648307,PZ373535,CM618607,WZ124696,PZ992459,PY715373,PZ809375,NG917998,YA500927,XZ992996,PY897641,NN059290,PY776103,BE513577,XN726833
    // - non unique
    //JW019790 - Fraction. Picking up wrong spouse award
    //BE957352 - Narration coming out wrong. First came eligible, then ineligible.
    @Test
    void debug() throws Exception {
        loader.init("PSA5");
        partitionGenerator.init();


        //val nino = "BE504618";
        val nino = "NS667637";

        //var account = loader.loadFromMongo(nino);
        var account = loader.loadFromPscsAtStage(nino, CalcCircElig); //CreateAc,CalcAcElig,PRSNTOPRSN,AWCM,GenCircs,CalcCircElig,CalcEnt
        //var account = loader.load_PRSN(nino);

        print(account);
        val i = 1;
    }

    //==================================
    @Autowired
    DebugLoader loader;


    //----------------------------------------
    private void print(Account ... accounts){
        val utils = new NUtils();
        var counter = 1;
        for(val ac : accounts) {
            try {
                val writer = new FileWriter("/Users/nilanjan.biswas/Dev/underpayments/debug/" +ac.getCitizenKey()+"_"+counter+".json");
                val json = utils.toJson(ac);
                writer.write(json);
                writer.close();
                counter ++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
