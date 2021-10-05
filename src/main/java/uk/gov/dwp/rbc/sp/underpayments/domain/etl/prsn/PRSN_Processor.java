package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;
import uk.gov.dwp.rbc.sp.underpayments.domain.logic.SpaLogic;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.*;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcStep;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.Problems;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.JsonUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.PscsUtils;

import javax.sql.DataSource;
import java.util.List;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.PRSN_INVALID_SEX;
import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.WarningFlags.Warning.*;

@Slf4j
public class PRSN_Processor implements ItemProcessor<R2575PRSN, Account> {

    private CryptoUtils cryptoUtils;

    private AppConfig config;

    private JdbcTemplate jdbc;

    private SpaLogic spaLogic;

    public PRSN_Processor(CryptoUtils cryptoUtils, SpaLogic spaLogic, AppConfig config, DataSource pscsDataSource) {
        this.cryptoUtils = cryptoUtils;
        this.spaLogic = spaLogic;
        this.config = config;
        this.jdbc = new JdbcTemplate(pscsDataSource);
    }

    //----------------------------------------------------
    @Override
    public Account process(R2575PRSN prsn) throws Exception {
        val account = new Account();
        account.setCalcResult(new CalcResult());

        account.tryLoad(acProblems -> {

            account.setCitizenKey(cryptoUtils.toCitizenKey(prsn.PK_R2575_NINO));
            account.setSex(getSex(prsn, acProblems));
            account.setDateOfBirth(PscsUtils.toDate(prsn.D2575_BTH_DT));

            //SPA date
            val spaDate = spaLogic.getDateOfSpa(account.getSex(), account.getDateOfBirth());
            account.setSpaDate(spaDate);

            account.setDateOfDeath(PscsUtils.toDate(prsn.D2575_DTH_DT));
            account.setIsDobVerified(prsn.D2575_BTH_DT_VFD_TP == 2);
            account.setIsDodVerified(prsn.D2575_DTH_DT_VFD_TP == 2);

            account.setXiAreaId(prsn.XIAREAID);
            account.setPkPrsn(prsn.PK_R2575_NINO);

            val citizen = new Citizen();
            citizen.setNino(prsn.PK_R2575_NINO);
            citizen.setNinoSuffix(prsn.D2575_NINO_SUFFIX);

            citizen.setName(getName(prsn, acProblems));

            Address address = getAddress(prsn, acProblems);
            citizen.setContactAddress(address);

            //Set if IG account based on country code on address
            if(! acProblems.warnings().isAnySet(PRSN_ADDRESS_MISSING, PRSN_COUNTRY_CODE_MISSING)) {
                account.setIsInternational(
                        !(
                            address.getCountryCode().equals("GBR")
                            || address.getCountryCode().equals("NI"))
                        );
            }

            //Set if IG based on frozen rate
            if(prsn.FRZN > 0){
                account.setIsInternational(true);
            }

            val citizenDataJson = JsonUtils.toJson(citizen);
            if (config.isTestData()) {
                account.setCitizenData(citizenDataJson);
                account.setEncrypted(false);
            } else {
                account.setCitizenData(cryptoUtils.encrypt(citizenDataJson));
                account.setEncrypted(true);
            }

            if(account.getIsDobVerified()) acProblems.warnings().set(PRSN_UNVERIFIED_DOB);

            //Relationship count
            account.setNumRelationships(prsn.RELN_CNT);

            //Is on PC
            account.setIsOnPC(prsn.PC_CNT > 0);

            //PRLBN
            account.setPkPrlbn(prsn.prlbn.PK_SRK_R2567);
            account.setPayDay(prsn.prlbn.D2567_BEN_PYDY_TP);

            //0 = Old Rules, 1 = New Rules  and 7 = Trans Protection.
            //Old and TP means paid in arrears, New means paid in advance
            account.setIsPaymentInAdvance(prsn.prlbn.D2567_PYDY_CONV_IND == 1);

            //0,1 = false,true
            account.setIsParkWeekPayment(prsn.prlbn.D2567_PART_WK_RP_IND == 1);

        });

        if(account.getProblems().getHasErrors()) {
            account.getCalcResult().setCode(CalcResult.Code.DATA_ERROR);
        }
        account.setStepCompleted(CalcStep.CREATE_ACCOUNT);

        return account;
    }

    //----------------------------------------------------------
    private Name getName(R2575PRSN prsn, Problems p) {
        val name = new Name();
        int ptr = 0;
        try {
            name.setTitle(getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_TITLE_TX_CNT));
            ptr += prsn.D2575_TITLE_TX_CNT;

            val firstName = getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_FRST_FNME_TX_CNT);
            if (firstName == "") p.warnings().set(PRSN_FIRST_NAME_MISSING);
            name.setFirstName(firstName);
            ptr += prsn.D2575_FRST_FNME_TX_CNT;

            name.setMiddleNames(getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_SUBSQ_FNME_TX_CNT));
            ptr += prsn.D2575_SUBSQ_FNME_TX_CNT;

            val surname = getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_SNME_TX_CNT);
            if (surname == "") p.warnings().set(PRSN_SURNAME_MISSING);
            name.setSurname(surname);
        }catch(StringIndexOutOfBoundsException sobe){
            p.warnings().set(PRSN_NAME_MALFORMED);
        }
        return name;
    }

    //----------------------------------------------------------
    private Address getAddress(R2575PRSN prsn, Problems p) {

        if(prsn.D2575_FRST_ADD_LNE_CNT > 0) {
            val address = new Address();
            int ptr = prsn.D2575_TITLE_TX_CNT
                    + prsn.D2575_FRST_FNME_TX_CNT
                    + prsn.D2575_SUBSQ_FNME_TX_CNT
                    + prsn.D2575_SNME_TX_CNT
                    + prsn.D2575_RQSTD_TITLE_TX_CNT;

            try {
                addAddressLine(address, getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_FRST_ADD_LNE_CNT));
                ptr += prsn.D2575_FRST_ADD_LNE_CNT;
                addAddressLine(address, getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_SCND_ADD_LNE_CNT));
                ptr += prsn.D2575_SCND_ADD_LNE_CNT;
                addAddressLine(address, getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_THRD_ADD_LNE_CNT));
                ptr += prsn.D2575_THRD_ADD_LNE_CNT;
                addAddressLine(address, getSubString(prsn.D2575_PERSON_D603, ptr, prsn.D2575_FRTH_ADD_LNE_CNT));
            }
            catch (Exception e){
                p.warnings().set(PRSN_ADDRESS_MALFORMED);
                throw e;
            }

            address.setPostCode(prsn.D2575_PST_CDE);
            val countryCode = CountryMapper.getIsoCode(prsn.D2575_RESID_CTRY_CDE_NO);
            if(countryCode == null || countryCode.equals("INVALID")){
                p.warnings().set(PRSN_COUNTRY_CODE_MISSING);
            }
            address.setCountryCode(countryCode);
            return address;
        }
        else {
            p.warnings().set(PRSN_ADDRESS_MISSING);
            return null;
        }
    }

    private String getSubString(String data, Integer ptr, int len) {
        val result = data.substring(ptr, ptr+len); //TODO StringIndexOutOfBoundsException
        return result.isBlank()? null : result;
    }

    private void addAddressLine(Address addr, String line) {
        if(line != null) addr.getLines().add(line);
    }

    //---------------------------------------------
    private Sex getSex(R2575PRSN prsn, Problems p) {
        if(prsn.D2575_SEX_FG.equals("M")) return Sex.M;
        else if(prsn.D2575_SEX_FG.equals("F")) return Sex.F;
        else {
            p.errors().set(PRSN_INVALID_SEX);
            return null;
        }
    }

    //-------------------------------------------------------------------------
    public List<R2575PRSN> loadPRSN(String nino) {

        val prsn_rowMapper = new PRSN_RowMapper();
        val prsnQuery = String.format(
                "select " + SELECT_CLAUSE +
                " from " + FROM_CLAUSE +
                " where PK_R2575_NINO ='%s'", nino);
        val prsnList = jdbc.query(prsnQuery, prsn_rowMapper);
        return prsnList;
    }

    public static final String SELECT_CLAUSE = "R2575PRSN.*, RELN.CNT as RELN_CNT, PC.CNT as PC_CNT, AWCMPRD.CNT as FRZN, PRLBN.*";
    public static final String FROM_CLAUSE = "R2575PRSN" +

            " left join (" + //To count the number of marriages
            "   select PKF_R2575_NINO, count(PKF_R2575_NINO) as CNT" +
            "   from R2576PRSNTOPRSN" +
            "   where D2576_REL_TP = 1" +
            "   group by PKF_R2575_NINO) RELN" +
            "   on PK_R2575_NINO = RELN.PKF_R2575_NINO" +

            " left join (" + //To see if this customer is on Pension Credit
            "   select PKF_R2575_NINO, count(PKF_R2575_NINO) as CNT" +
            "   from R2526CPLOC" +
            "   where D2526_CP_TP = 3" +
            "   group by PKF_R2575_NINO) PC" +
            "   on PK_R2575_NINO = PC.PKF_R2575_NINO" +

            " left join (" + //To check if rates are frozen, hence Int'l
            "   select PKF_R2575_NINO, count(PKF_R2575_NINO) as CNT" +
            "   from R2509AWCMPRD" +
            "   where D2509_BEN_CM_UPRTG_DT > 0" +
            "   group by PKF_R2575_NINO) AWCMPRD" +
            "   on PK_R2575_NINO = AWCMPRD.PKF_R2575_NINO" +

            " left join (" + //To get personal benefit details
            "   select PKF_R2575_NINO, PK_SRK_R2567, D2567_BEN_TP, D2567_BEN_PYDY_TP,"+
            "     D2567_PART_WK_RP_IND, D2567_PYDY_CONV_IND" +
            "   from R2567PRLBN" +
            "   where D2567_BEN_TP = 'RP') PRLBN" +
            "   on PK_R2575_NINO = PRLBN.PKF_R2575_NINO";
}
