package uk.gov.dwp.rbc.sp.underpayments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@ConfigurationProperties(prefix = "underpayments")
@Getter @Setter
public class AppConfig {
    private String calcDate;
    private String kmsKeyArn;
    private boolean testData;
    private int batchJobThreads;
    private int batchJobChunkSize;

    public int getCalcJobThreads() {
        return batchJobThreads;
    }

    public OffsetDateTime getCalcDate() {
        return OffsetDateTime.parse(calcDate+"T10:15:30+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
