package uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Relationship;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.WarningFlags;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.CryptoUtils;
import uk.gov.dwp.rbc.sp.underpayments.utils.PscsUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.ErrorFlags.Error.*;

@Slf4j
public class PRSNTOPRSN_Processor implements ItemProcessor<Account, Account> {


    private CryptoUtils cryptoUtils;

    private JdbcTemplate jdbc;

    private AccountRepo accountRepo;

    //----------------------------------------------------
    public PRSNTOPRSN_Processor(CryptoUtils cryptoUtils,
                                DataSource pscsDataSource,
                                AccountRepo accountRepo) {
        this.cryptoUtils = cryptoUtils;
        this.jdbc = new JdbcTemplate(pscsDataSource);
        this.accountRepo = accountRepo;
    }

    //----------------------------------------------------
    @Override
    public Account process(Account account) throws Exception {

        if(account.getNumRelationships() == 0) return account; //No relationships

        account.tryLoad(acProblems -> {

            val prsntoprsnList = loadPRSNTOPRSN(account);

            val startDates = new HashMap<Integer, ArrayList<R2576PRSNTOPRSN>>();

            //Clean out spurious R2576PRSNTOPRSNs
            for(val prsntoprsn: prsntoprsnList) {
                val stDt = prsntoprsn.D2576_REL_STRT_DT;
                if( stDt ==0 || stDt == 65535 ||prsntoprsn.D2576_REL_END_DT == 65535) {
                    acProblems.errors().set(ErrorFlags.Error.PRSNTOPRSN_HAS_DATE_PROBLEMS);
                }
                //Gather all start date-prsnprn sets
                if(!startDates.containsKey(stDt)){
                    val l = new ArrayList<R2576PRSNTOPRSN>();
                    l.add(prsntoprsn);
                    startDates.put(stDt, l);
                }
                else startDates.get(stDt).add(prsntoprsn);
            }

            val okList = new ArrayList<R2576PRSNTOPRSN>();
            for(val stDt: startDates.keySet()){
                val p2pList = startDates.get(stDt);
                //If only one item with this start date, then OK, keep it
                if(p2pList.size() == 1){
                    okList.add(p2pList.get(0));
                }
                else { //If more than one items with this start date
                    //then keep the one with an end date and reject the rest
                    acProblems.warnings().set(WarningFlags.Warning.PRSNTOPRSN_DUPLICATES);
                    for(val prsntoprsn: p2pList) {
                        if(prsntoprsn.D2576_REL_END_DT != 0) {
                            okList.add(prsntoprsn);
                            break;
                        }
                    }
                }
            }

            //Now populate Relationships
            val relationships = new ArrayList<Relationship>();
            for(val prsntoprsn: okList){

                val relationship = new Relationship();
                relationship.tryLoad(relnProblems -> {

                    relationship.setStartDate(PscsUtils.toDate(prsntoprsn.D2576_REL_STRT_DT));
                    relationship.setEndDate(PscsUtils.toDate(prsntoprsn.D2576_REL_END_DT));

                    relationship.setStartVerified(getStartVerified(prsntoprsn));
                    relationship.setEndVerified(getEndVerified(prsntoprsn));
                    relationship.setEndReason(getEndReason(prsntoprsn));

                    relationship.setPkPrsnB(prsntoprsn.D2576_NINO_B);
                    relationship.setPkPrsnToPrsn(prsntoprsn.PK_SRK_R2576);

                    if (prsntoprsn.D2576_NINO_B != null) {
                        val otherCK = cryptoUtils.toCitizenKey(prsntoprsn.D2576_NINO_B);
                        relationship.setCitizenKey(otherCK);

                        val spouseAcOpt = accountRepo.findByPkPrsn(prsntoprsn.D2576_NINO_B);
                        if (!spouseAcOpt.isPresent()) {
                            relnProblems.errors().set(RELN_SPOUSE_AC_MISSING);
                        }
                    }
                    else {
                        relnProblems.errors().set(PRSNTOPRSN_SPOUSE_NINO_MISSING);
                    }
                });

                relationships.add(relationship);

                if(relationship.getProblems().getHasErrors()){
                    acProblems.errors().set(AC_RELN_HAS_ERRORS);
                }
            }

            account.setRelationships(relationships);
        });

        if(account.getProblems().getHasErrors()) {
            account.getCalcResult().setCode(CalcResult.Code.DATA_ERROR);
        }

        account.getCalcResult().setRelnsLoaded(true);
        return account;
    }

    //------------------------------------------------------------------------
    private Relationship.EndReason getEndReason(R2576PRSNTOPRSN prsntoprsn) {
        switch (prsntoprsn.D2576_REL_END_RSN_TP) {
            case 0:
                return Relationship.EndReason.NA;
            case 1:
                return Relationship.EndReason.DEATH;
            case 2:
                return Relationship.EndReason.DIVORCE;
            case 3:
            case 4:
                return Relationship.EndReason.MARRIAGE_VOID;
            case 5:
                return Relationship.EndReason.SEPARATED;
            default:
                return Relationship.EndReason.OTHER;
        }
    }

    //-------------------------------------------------------------
    private boolean getStartVerified(R2576PRSNTOPRSN prsntoprsn) {

        return prsntoprsn.D2576_VFD_REL_STRT_TP == 2
                || prsntoprsn.D2576_VFD_REL_STRT_TP == 3;
    }

    //-----------------------------------------------------------
    private boolean getEndVerified(R2576PRSNTOPRSN prsntoprsn) {

        return (prsntoprsn.D2576_VFD_REL_END_TP == 2
                || prsntoprsn.D2576_VFD_REL_END_TP == 3);
    }

    //-------------------------------------------------------------------------
    public List<R2576PRSNTOPRSN> loadPRSNTOPRSN(Account account) {

        val prsnToPrsn_rowMapper = new PRSNTOPRSN_RowMapper();

        val prsnToPrsnQuery = String.format
                ("select XIAREAID,PK_SRK_R2576,PKF_R2575_NINO,D2576_NINO_B,D2576_REL_TP,D2576_REL_STRT_DT,"+
                        "D2576_REL_END_DT,D2576_REL_END_RSN_TP,D2576_VFD_REL_STRT_TP,D2576_VFD_REL_END_TP" +
                        " from R2576PRSNTOPRSN"+
                        " where D2576_REL_TP = 1" +
                        " and PKF_R2575_NINO='%s'", account.getPkPrsn());
        return jdbc.query(prsnToPrsnQuery, prsnToPrsn_rowMapper);
    }

}
