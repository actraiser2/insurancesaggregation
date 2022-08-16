package com.fpnatools.aggregation.insurances.framework.healthIndicators;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import static io.restassured.RestAssured.*;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class SeleniumGridHealthIndicator implements HealthIndicator {

	private Environment env;
	
	@Override
	public Health health() {
		// TODO Auto-generated method stub
		String seleniumGridUrl = env.getProperty("selenium.grid.url");
		log.info("Checking the health of the system");
		int responseStatus = 0;
		Health health = null;
		try {
			given().
				log().all().
			when().
				get(seleniumGridUrl).
			then().
				statusCode(200);
			health = Health.up().build();
		}
		catch(Exception ex) {
			log.error("Error checking the system:", ex);
			health = Health.down().withException(ex).build();
		}
		
		
		
		return health;
	}

}
