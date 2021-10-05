package uk.gov.dwp.rbc.sp.underpayments.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
public class FileUtils {

    //-------------------------------------------------------------------------------
    public static void processResourceFile(String file, Consumer<String> processor ) {

        log.info("Reading "+file);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(file);

        val streamReader = new InputStreamReader(inputStream, UTF_8);
        val reader = new BufferedReader(streamReader);
        try {
            for (String line; (line = reader.readLine()) != null; ) {
                processor.accept(line);
            }
        }
        catch (Exception e) {
            throw new UeException("Failed to read "+file+" from resources folder", e);
        }
    }
}
