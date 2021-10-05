package uk.gov.dwp.rbc.sp.underpayments.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "pscs")
@Getter @Setter
public class PscsData {

    private String dbUrl;
    private String dbPassword;
    private List<String> schemas;

    private Map<String, DataSource> dsMap = new HashMap<>();

    //---------------------------------------------
    public DataSource getDataSource(String schema){
        if(!dsMap.containsKey(schema)) {
            throw new RuntimeException("Unknown schema "+ schema);
        }
        return dsMap.get(schema);
    }

    //---------------------------------------------
    public DataSource createDataSource(String schema){
        val oraProps = new Properties();
        oraProps.put("oracle.net.CONNECT_TIMEOUT", 60000);
        oraProps.put("oracle.net.READ_TIMEOUT", 60000);
        oraProps.put("oracle.jdbc.ReadTimeout", 60000);
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(schema+"_USER");
        ds.setPassword(dbPassword);
        ds.setDataSourceProperties(oraProps);
        ds.setConnectionTimeout(60000);
        ds.setMaximumPoolSize(20);
        return ds;
    }

    //-----------------------------
    @PostConstruct
    void init() {
        for(val schema : schemas){
            val ds = createDataSource(schema);
            dsMap.put(schema, ds);
        }
    }
}
