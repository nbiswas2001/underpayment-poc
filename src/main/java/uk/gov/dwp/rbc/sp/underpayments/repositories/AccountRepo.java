package uk.gov.dwp.rbc.sp.underpayments.repositories;

import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.calc.CalcResult;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.util.Optional;

public interface AccountRepo extends MongoRepository<Account, String> {

    Optional<Account> findByPkPrsn(String nino);

    Optional<Account> findByCitizenKey(String citizenKey);

    @Query(value = "{ 'ageCategory':?1, 'calcResult.code': ?2 }")
    Page<Account> withAgeCategoryAndResultCode(PageRequest pgRequest, String ageCategory, String code);

    @Query(value = "{ 'calcResult.code':?1, 'calcResult.reason': ?2}")
    Page<Account> withResultCodeAndReason(PageRequest pgRequest, String code, String reason);

    @Query(value = "{'problems.hasErrors': true }")
    Page<Account> withErrors(PageRequest pgRequest);

    @Query(value = "{'problems.hasWarnings': true }")
    Page<Account> withWarnings(PageRequest pgRequest);
}
