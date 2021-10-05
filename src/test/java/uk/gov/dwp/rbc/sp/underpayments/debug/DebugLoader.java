package uk.gov.dwp.rbc.sp.underpayments.debug;

import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.config.PscsData;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm.AW_AWCM_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.PRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn.PRSNTOPRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.CircsLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EligibilityLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.EntitlementLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.SpaLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.services.narrator.NAccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static uk.gov.dwp.rbc.sp.underpayments.debug.DebugLoader.Stage.*;

@Component
public class DebugLoader {

    @Autowired
    EligibilityLogic eligibilityLogic;

    CircsLogic circsLogic;

    @Autowired
    EntitlementLogic entitlementLogic;

    NAccountRepo accountRepo;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    SpaLogic spaLogic;

    @Autowired
    PscsData pscsData;

    private PRSN_Processor prsn_processor;
    private PRSNTOPRSN_Processor prsntoprsn_processor;
    private AW_AWCM_Processor aw_awcm_processor;

    //--------------------------------
    public void init(String schema) {
        val ds = pscsData.createDataSource(schema);
        prsn_processor = new PRSN_Processor(cryptoUtils, spaLogic, appConfig, ds);
        aw_awcm_processor = new AW_AWCM_Processor(ds);
        accountRepo = new NAccountRepo(prsn_processor, aw_awcm_processor);
        prsntoprsn_processor = new PRSNTOPRSN_Processor(cryptoUtils, ds, accountRepo);
        circsLogic = new CircsLogic(appConfig, accountRepo);
    }
    //----------------------------------------------
    public Account loadFromMongo(String nino){
        val acOpt = accountRepo.findByPkPrsn(nino);
        if(acOpt.isEmpty()) throw new RuntimeException("NINO not found - "+nino);
        else return acOpt.get();
    }

    //------------------------------------------------
    public Account loadFromPscsAtStage(String nino, Stage stage) throws Exception{
        Account ac;
        switch (stage){
            case CreateAc:
                val prsnList = prsn_processor.loadPRSN(nino);
                if(prsnList.size() == 0) throw new RuntimeException("Unable to find nino "+nino);
                ac = prsn_processor.process(prsnList.get(0));
                return ac;
            case CalcAcElig:
                ac = loadFromPscsAtStage(nino, CreateAc);
                check(ac);
                eligibilityLogic.setEligibilityBasedOnAccount(ac);
                return ac;
            case PRSNTOPRSN:
                ac = loadFromPscsAtStage(nino, CreateAc);
                check(ac);
                prsntoprsn_processor.process(ac);
                return ac;
            case AWCM:
                ac = loadFromPscsAtStage(nino, CreateAc);
                check(ac);
                aw_awcm_processor.process(ac);
                return ac;
            case GenCircs:
                ac = loadFromPscsAtStage(nino, CalcAcElig);
                check(ac);
                prsntoprsn_processor.process(ac);
                check(ac);
                aw_awcm_processor.process(ac);
                check(ac);
                circsLogic.createCircs(ac);
                return ac;
            case CalcCircElig:
                ac = loadFromPscsAtStage(nino, GenCircs);
                check(ac);
                eligibilityLogic.setEligibilityBasedOnCircs(ac);
                return ac;
            case CalcEnt:
                ac = loadFromPscsAtStage(nino, CalcCircElig);
                check(ac);
                entitlementLogic.calculateEntitlement(ac);
                return ac;
            default:
                throw new NotImplementedException();
        }
    }

    //------------------
    private void check(Account ac){
        if(ac.getProblems().getHasErrors()) {
            throw new RuntimeException("Account has errors");
        }
    }

    //-----------------
    enum Stage {
        CreateAc,
        CalcAcElig,
        PRSNTOPRSN,
        AWCM,
        GenCircs,
        CalcCircElig,
        CalcEnt
    }

}
