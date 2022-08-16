package com.fpnatools.aggregation.insurances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InsurancesAggregationApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(InsurancesAggregationApplication.class);
		app.setLogStartupInfo(true);
		app.run(args);
	}

}
