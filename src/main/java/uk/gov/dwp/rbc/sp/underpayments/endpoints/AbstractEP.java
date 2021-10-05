package uk.gov.dwp.rbc.sp.underpayments.endpoints;

import org.springframework.data.domain.PageRequest;
import uk.gov.dwp.rbc.sp.underpayments.endpoints.gql.GqlPageResponse;

import java.util.Optional;

public class AbstractEP {

    //-----------------------------------------------------------------------
    protected PageRequest getPageRequest(Optional<Integer> pageNumOpt,
                                         Optional<Integer> pageSizeOpt) {
        var pgNum = pageNumOpt.orElse(0);
        var pgSize = pageSizeOpt.orElse(GqlPageResponse.DEFAULT_PAGE_SIZE);
        if(pgSize > GqlPageResponse.MAX_PAGE_SIZE) pgSize = GqlPageResponse.MAX_PAGE_SIZE;
        return PageRequest.of(pgNum, pgSize);
    }
}
