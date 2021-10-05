package uk.gov.dwp.rbc.sp.underpayments.utils;

import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JsonUtils {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    //--------------------------------------------------
    public static String toJson(Object o) {
        if(o == null) return null;
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new UeException("Failed to convert "+o.getClass().getSimpleName()+" to JSON");
        }
    }

    //-----------------------------------------------------------------------------------------------
    public static <T> T fromJson(String json, Class<T> clazz) {
        T result = null;
        if(!(json == null || json.isBlank())) {
            try {
                result = mapper.readValue(json, clazz);
            } catch (JsonProcessingException e) {
                throw new UeException("Failed to create "+clazz.getSimpleName()+" from JSON - " + json);
            }
        }
        return result;
    }

    //-----------------------------------------------------------
    public static String deflate(String inputStr){
        if(inputStr == null) return null;
        try {
            deflater.reset();
            val input = inputStr.getBytes(StandardCharsets.UTF_8);
            val output = ByteBuffer.allocate(1024);
            deflater.setInput(input);
            deflater.finish();
            int l = deflater.deflate(output);
            return Base64.encodeAsString(Arrays.copyOfRange(output.array(), 0, l));

        } catch(Exception e) {
            throw new UeException("Could not deflate string", e);
        }
    }
    private static final Deflater deflater = new Deflater(Deflater.BEST_SPEED);

    //---------------------------------------------------------------
    public static String inflate(String inputStr){
        if(inputStr == null) return null;
        try {
            inflater.reset();
            val input = Base64.decode(inputStr.getBytes(StandardCharsets.UTF_8));
            val output = ByteBuffer.allocate(20480);
            inflater.setInput(input, 0, input.length);
            int l = inflater.inflate(output);
            return new String(output.array(), 0, l, StandardCharsets.UTF_8);
        } catch(Exception e) {
            throw new UeException("Could not inflate string", e);
        }
    }
    private static final Inflater inflater = new Inflater();

}
