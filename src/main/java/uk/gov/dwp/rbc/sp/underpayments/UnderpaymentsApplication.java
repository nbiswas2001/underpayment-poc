package uk.gov.dwp.rbc.sp.underpayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class UnderpaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnderpaymentsApplication.class, args);
	}

}
