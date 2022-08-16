package com.fpnatools.aggregation.insurances;

import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:insurances-aggregation"})
@Slf4j
@ActiveProfiles("test")
class InsurancesAggregationApplicationTests {

	@Autowired DataSource datasource;
	@Autowired InsuranceCompanyRepository insuranceCompanyRepository;
	@Autowired ExecutionRepository executionRepository;
	
	@Test
	void checkDataSource() throws SQLException, InterruptedException {
		Assert.isTrue(datasource != null, "Datasource is null");
		log.warn("Database detected:" + datasource.getConnection().getMetaData().getDatabaseProductName());
		
	} 
	
	@Test
	public void checkInsuranceCompanies() {
		Assert.isTrue(insuranceCompanyRepository.count() > 0, "The table company insurance can't be empty" );
	}
	
	@Test
	@Transactional
	public void testGetAllInsuranceCompanies() {
		insuranceCompanyRepository.streamAllByTimestampGreaterThanOrderByName(LocalDateTime.now().minusYears(2)).
			forEach(c -> {
				log.info(c.toString());
		});
	}
	
	@Test
	public void testUpdateInsuranceCompany() {
		insuranceCompanyRepository.updateName(1l, "Casablanca");
		
		Assert.isTrue(insuranceCompanyRepository.findByName("Casablanca").isPresent(), 
				"The company Casabalnca doesn't exist");
	}
	
	@Test
	public void testExecutionProyected() {
		executionRepository.findAllExecutionsProjected().
			stream().
			forEach(e -> {
				log.info(e.execution() + " -> " + e.name());
			});
	}

}
