package uk.gov.dwp.rbc.sp.underpayments.services.narrator;

import lombok.val;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.aw_awcm.AW_AWCM_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsn.PRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.etl.prsntoprsn.PRSNTOPRSN_Processor;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.util.List;
import java.util.Optional;

public class NAccountRepo implements AccountRepo {

    private PRSN_Processor prsn_processor;
    private AW_AWCM_Processor aw_awcm_processor;


    public NAccountRepo(PRSN_Processor prsn_processor,
                        AW_AWCM_Processor aw_awcm_processor) {
        this.prsn_processor = prsn_processor;
        this.aw_awcm_processor = aw_awcm_processor;
    }

    @Override
    public Optional<Account> findByPkPrsn(String nino) {
        val prsnList = prsn_processor.loadPRSN(nino);
        if(prsnList.size() == 0) throw new UeException("NINO "+nino+" not in PSCS DB");
        val prsn = prsnList.get(0);
        Account ac = null;
        try {
            ac = prsn_processor.process(prsn);
            aw_awcm_processor.process(ac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(ac);
    }

    @Override
    public Optional<Account> findByCitizenKey(String citizenKey) {
        return Optional.empty();
    }

    @Override
    public Page<Account> withAgeCategoryAndResultCode(PageRequest pgRequest, String ageCategory, String code) {
        return null;
    }

    @Override
    public Page<Account> withResultCodeAndReason(PageRequest pgRequest, String code, String reason) {
        return null;
    }

    @Override
    public Page<Account> withErrors(PageRequest pgRequest) {
        return null;
    }

    @Override
    public Page<Account> withWarnings(PageRequest pgRequest) {
        return null;
    }

    @Override
    public <S extends Account> S save(S s) {
        return null;
    }

    @Override
    public <S extends Account> List<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public Optional<Account> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public List<Account> findAll() {
        return null;
    }

    @Override
    public Iterable<Account> findAllById(Iterable<String> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(Account account) {

    }

    @Override
    public void deleteAll(Iterable<? extends Account> iterable) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Account> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Account> S insert(S s) {
        return null;
    }

    @Override
    public <S extends Account> List<S> insert(Iterable<S> iterable) {
        return null;
    }

    @Override
    public <S extends Account> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Account> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Account> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Account> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Account> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Account> boolean exists(Example<S> example) {
        return false;
    }


}
