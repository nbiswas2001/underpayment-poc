package uk.gov.dwp.rbc.sp.underpayments.services.narrator;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.domain.model.Account;

import java.text.SimpleDateFormat;

@Component
public class NUtils {

    private ObjectMapper mapper ;
    private DefaultPrettyPrinter printer;

    public NUtils() {

        printer = new DefaultPrettyPrinter();
        val indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy")); //TODO - This is not working
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    //-----------------------------------
    public String toJson(Object o){
        try {
            return mapper.writer(printer).writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing JSON", e);
        }
    }

    //-----------------------------------
    public Account cloneAccount(Account a){
        val json = toJson(a);
        try {
            val clone = mapper.readValue(json, Account.class);
            return clone;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading JSON", e);
        }
    }
}
