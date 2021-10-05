package uk.gov.dwp.rbc.sp.underpayments.services.narrator;

import com.github.mustachejava.DefaultMustacheFactory;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm.AW_AWCM_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.awcm.AWCM_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.PRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn.PRSNTOPRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.CircsLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EntitlementLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.SpaLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Relationship;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.SpAward;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SpSubAwardComponent;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent.SubAwardComponent;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Circumstance;
import uk.gov.dwp.rbc.sp.underpayments.jobs.calc_ac_elig.AccountPartitionGenerator;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.PscsUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NarratorService {

    @Autowired
    EligibilityLogic eligibilityLogic;

    @Autowired
    NUtils utils;

    @Autowired
    EntitlementLogic entitlementLogic;

    @Autowired
    SpaLogic spaLogic;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    AppConfig appConfig;

    NAccountRepo accountRepo;

    @Autowired
    PscsData pscsData;

    @Autowired
    AccountPartitionGenerator partitionGenerator;

    CircsLogic circsLogic;

    private PRSN_Processor prsn_processor;
    private PRSNTOPRSN_Processor prsntoprsn_processor;
    private AWCM_Processor awcm_processor;
    private AW_AWCM_Processor aw_awcm_processor;

    private static final DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmz");
    private static final DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    //--------------------------------------------------------------------
    public String getNarration(String schema, String nino) throws Exception {

        val ds = pscsData.createDataSource(schema);
        prsn_processor = new PRSN_Processor(cryptoUtils, spaLogic, appConfig, ds);
        awcm_processor = new AWCM_Processor(ds);
        aw_awcm_processor = new AW_AWCM_Processor(ds);
        accountRepo = new NAccountRepo(prsn_processor, aw_awcm_processor);
        prsntoprsn_processor = new PRSNTOPRSN_Processor(cryptoUtils, ds, accountRepo);
        circsLogic = new CircsLogic(appConfig, accountRepo);
        partitionGenerator.init();

        //Set up mustache
        val mf = new DefaultMustacheFactory();
        val m = mf.compile("narrator/narration.html");
        val data = new HashMap<String, Object>();

        //Template lambdas
        data.put("dt", (Function<String, String>) dt -> dt.isBlank() ? "" : fmt2.format(OffsetDateTime.parse(dt,DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        data.put("pscsDt", (Function<String, String>) i -> {
            val dt = PscsUtils.toDate(Integer.parseInt(i));
            if (dt == null) return "";
            else return new StringBuilder()
                    .append(i).append("(").append(fmt2.format(dt)).append(")")
                    .toString();
        });

        //********** TEMPLATE DATA ************//

        //0. NINO
        data.put("nino", nino);

        //1. PRSN + Account
        val ac1 = narratePRSN(nino, data);

        //2. AWCMs (+ AWs for reference)
        val ac2 = narrateAWCMs(ac1, data);

        //3. PRSNTOPRSN + Relationships
        val ac3 = narratePRSNTOPRSN(ac2, data);

        //4. Spouse Accounts
        narrateSpouseData(ac3, data);

        //5. Ac Eligibility
        val ac4 = narrateAcElig(ac3, data);

        //If 'maybe eligible'
        val maybeEligible = ac4.getCalcResult().getCode().equals(CalcResult.Code.MAYBE_ELIGIBLE);
        data.put("maybeEligible", maybeEligible);
        if (maybeEligible) {

            //6. Circs Eligibility
            val ac5 = narrateCircsElig(ac4, data);

            //If 'eligible'
//            val eligible = ac5.getCalcResult().getCode().equals(CalcResult.Code.ELIGIBLE);
//            data.put("eligible", eligible);
//            if (eligible) {
//                //7. Entitlement
//                val ac7 = narrateEnt(ac6, data);
//            }
        }
        //********** END ************//

        //Render templates
        val writer = new StringWriter();
        try {
            m.execute(writer, data).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    //---------------------------------------------------------------------
    private Account narratePRSN(String nino, HashMap<String, Object> data) throws Exception {
        val prsnList = prsn_processor.loadPRSN(nino);
        if(prsnList.size() == 0) {
            throw new RuntimeException("NINO "+nino+" not present in DB. Check if connecting to correct shard");
        }
        data.put("prsnList", prsnList);
        val prsn = prsnList.get(0);
        val ac1 = prsn_processor.process(prsn);
        data.put("ac1", ac1);

        data.put("ageCategory", spaLogic.getAgeCategory(ac1, appConfig.getCalcDate()));

        return ac1;
    }

    //---------------------------------------------------------------------
    private Account narrateAWCMs(Account ac1, HashMap<String, Object> data) throws Exception {

        val awList = awcm_processor.loadAW(ac1);
        data.put("awList", awList);
        val awcmList = awcm_processor.loadAWCM(ac1);
        data.put("awcmList", awcmList);
        val ac2 = utils.cloneAccount(ac1);
        aw_awcm_processor.process(ac2);
        val counter2 = new int[]{1};
        val awardList = ac2.getAwards().stream()
                .sorted(Comparator.comparing(SpAward::getStartDate))
                .map(a -> new NListItem<SpAward>(counter2[0]++, a))
                .collect(Collectors.toList());
        data.put("awardList", awardList);
        return ac2;
    }

    //---------------------------------------------------------------------
    private Account narratePRSNTOPRSN(Account ac2, HashMap<String, Object> data) throws Exception {
        val ac3 = utils.cloneAccount(ac2);
        val prsntoprsnList = prsntoprsn_processor.loadPRSNTOPRSN(ac3);
        data.put("prsntoprsnList", prsntoprsnList);
        prsntoprsn_processor.process(ac3);
        val counter1 = new int[]{1};
        val relnList = ac3.getRelationships()
                .stream()
                .filter(r -> r.getPkPrsnB() != null && r.getStartDate() !=null)
                .sorted(Comparator.comparing(Relationship::getStartDate))
                .map(r -> new NListItem<Relationship>(counter1[0]++, r))
                .collect(Collectors.toList());
        data.put("relnList", relnList);

        data.put("hasRelns", relnList.size() > 0);
        return ac3;
    }


    //---------------------------------------------------------------------
    private void narrateSpouseData(Account ac3, HashMap<String, Object> data) throws Exception{
        val spDataList = new ArrayList<NSpouseData>();
        int idx = 1;
        for(val reln: ac3.getRelationships()){
            val spData = new NSpouseData(idx++);
            val spNino = reln.getPkPrsnB();
            if(spNino == null) {
                throw new RuntimeException("Spouse NINO missing on PRSNTOPRSN");
            }
            val prsnList = prsn_processor.loadPRSN(spNino);
            if(prsnList.size() == 0) {
                throw new RuntimeException("Spouse NINO "+spNino+" not present in DB. Check if connecting to correct shard");
            }
            val prsn = prsnList.get(0);
            val spAccount = prsn_processor.process(prsn);
            val aws = awcm_processor.loadAW(spAccount);
            val awcms = awcm_processor.loadAWCM(spAccount);

            aw_awcm_processor.process(spAccount);

            spData.spAccount = spAccount;
            spData.AWs = aws;
            spData.AWCMs = awcms;

            val spAwards = new ArrayList<SpAward>();
            for (val aw : spAccount.getAwards().stream()
                    .sorted(Comparator.comparing(SpAward::getStartDate))
                    .collect(Collectors.toList())) {

                val sacs = new ArrayList<SubAwardComponent>();
                for (val sac : aw.getSubAwardComponents()) {
                    if (sac instanceof SpSubAwardComponent) {
                        sac.setRawData(null);
                        sacs.add(sac);
                    }
                }
                aw.setSubAwardComponents(sacs);

                spAwards.add(aw);
            }

            spData.spAwards = spAwards;

            spDataList.add(spData);

        }
        data.put("spDataList", spDataList);
    }

    //---------------------------------------------------------------------
    private Account narrateAcElig(Account ac3, HashMap<String, Object> data) {
        val ac4 = utils.cloneAccount(ac3);
        eligibilityLogic.setEligibilityBasedOnAccount(ac4);
        data.put("ac4", ac4);
        return ac4;
    }

    //---------------------------------------------------------------------
    private Account narrateEnt(Account ac6, HashMap<String, Object> data) {
        val ac7 = utils.cloneAccount(ac6);
        entitlementLogic.calculateEntitlement(ac7);

        val entCircList = ac7.getCircumstances().stream()
                .filter(c -> c.getCalcResult().getCode().equals(CalcResult.Code.ENTITLED))
                .collect(Collectors.toList());

        data.put("entCircList", entCircList);
        data.put("underpaidAmount", ac7.getCalcResult().getUnderpaidAmount());
        return ac7;
    }

    //------------------------------------------------------------
    Account narrateCircsElig(Account ac4, HashMap<String, Object> data) throws Exception {
        val ac5 = utils.cloneAccount(ac4);
        circsLogic.createCircs(ac5);
        eligibilityLogic.setEligibilityBasedOnCircs(ac5);

        val circs = ac5.getCircumstances().stream()
                .sorted(Comparator.comparing(Circumstance::getStartDate))
                .collect(Collectors.toList());

        data.put("circs", circs);
        data.put("ac5", ac5);

        return ac5;
    }
}