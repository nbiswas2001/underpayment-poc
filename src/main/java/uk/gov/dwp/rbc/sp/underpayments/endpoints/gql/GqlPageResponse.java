package uk.gov.dwp.rbc.sp.underpayments.endpoints.gql;

import lombok.ToString;
import lombok.val;
import org.springframework.data.domain.Page;

import java.util.List;

@ToString
public class GqlPageResponse<T> {

    public List<T> items;
    public int totalItems;
    public int totalPages;

    public static final int DEFAULT_PAGE_SIZE = 25;
    public static final int MAX_PAGE_SIZE = 100;

    public static <T> GqlPageResponse<T> of(Page<T> page) {
        val resp = new GqlPageResponse<T>();
        resp.totalPages = page.getTotalPages();
        resp.totalItems = (int) page.getTotalElements();
        resp.items = page.getContent();
        return resp;
    }

}
