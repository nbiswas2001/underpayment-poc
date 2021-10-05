package uk.gov.dwp.rbc.sp.underpayments.endpoints.gql;

import lombok.val;

import java.util.Optional;

public class GqlResponse<T> {
    public T item;

    public static <T> GqlResponse<T> of(Optional<T> itemOpt) {
        val resp = new GqlResponse<T>();
        if(itemOpt.isPresent()){
            resp.item = itemOpt.get();
        }
        return resp;
    }

}
