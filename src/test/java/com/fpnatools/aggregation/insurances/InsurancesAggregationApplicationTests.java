package com.fpnatools.aggregation.insurances;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fpnatools.aggregation.insurances.application.ports.input.CreateExecutionInputPort;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {"spring.datasource.url=jdbc:h2:mem:insurances-aggregation"})
@Slf4j
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class InsurancesAggregationApplicationTests {

	@Autowired DataSource datasource;
	@Autowired InsuranceCompanyRepository insuranceCompanyRepository;
	@Autowired ExecutionRepository executionRepository;
	@Autowired WebTestClient webClient;
	@MockBean CreateExecutionInputPort createExecutionInputPort;
	
	@BeforeEach
	public void configureMockCreateExecutionInputPort() {
		Mockito.when(createExecutionInputPort.
				createExecution(Mockito.any(), Mockito.any())).thenReturn(1000l);
	}
	
	@Test
	@DisplayName("Evaluating datasource is not null")
	void checkDataSource() throws SQLException, InterruptedException {
		assertNotNull(datasource.getConnection());
		
	} 
	
	
	
	@Test
	@Transactional
	@DisplayName("Insurances Company table has n elements")
	public void testGetAllInsuranceCompanies() {
		assertTrue(insuranceCompanyRepository.count() > 0, () -> "Table insurance company can not be empty");
	}
	
	
	
	@DisplayName("Evaluating insurance company exists for each data")
	@ParameterizedTest
	@CsvFileSource(resources = "/insurance-companies.csv", delimiterString = ",")
	public void testInsuranceCompanyExist(int expectedValue, String insuranceCompany) {
		log.info("Evaluating " + insuranceCompany + " to " + expectedValue);
		if (expectedValue == 1) {
			assertTrue(insuranceCompanyRepository.findByName(insuranceCompany).isPresent(), () -> insuranceCompany + " has to exist");
		}
		else {
			assertTrue(insuranceCompanyRepository.findByName(insuranceCompany).isEmpty(), () -> insuranceCompany + " hasn't to exist");
		}
	}
	
	@DisplayName("Test executionId is created")
	@Test
	public void testExecutionId() {
		String body = """
				{
				    "entityName":"Mutuactivos",
				    "credentials":{
				        "username":"5303333496V",
				        "password":"Norita1973"
				     }
		        }
		     """;
		
		String accessToken = "eyJraWQiOiIyNDMwNDliYy1kNzc5LTRkMmEtOWE5Yy1hMGVlYWQ4YTQyMTkiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6ImFkbWluIiwibmJmIjoxNjY1MjUzODUwLCJzY29wZSI6WyJpbnN1cmFuY2VzIl0sImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo5MDkxIiwiZXhwIjoxNjY1MzQwMjUwLCJpYXQiOjE2NjUyNTM4NTB9.TzCqV01MXYJRs_fmanAsZj_JdbtTJrm121VbxASbce6LtTyLy2sNZZlg7TvzQHnuk4BUtmrTC4TEGoEsVnZ2zjPsOptknjNM8J9La41G0MR1kfau4GE8LwMY96RU27a2BuL8JOMzbk2pr68kZ7OmvGIgaiadvGGbBEw9-kbJeRz1555ZAP0KmzUNM3STTPvYFSECTG4nRXPaVpZy9hrj074-n8Ngocp9byyZHb3fXKLC02-TOzEMQ3CRuvrjcw9Ubx8uC94roDFH9iuGCQWN_zQqbgxi5pdhZNZ9_fv9HKxLssHU5cW3x39F9_uWMDsfNRPthKKr46rmB_6iqYiaaQ";

		webClient.post().uri("/api/executions").bodyValue(body).
			header("Authorization", "Bearer " + accessToken).
			header("Content-Type", "application/json").
			exchange().
				expectStatus().isCreated().
				expectBody().jsonPath("executionId").isNotEmpty();
	}
	
	@DisplayName("Test Mock createExecutionInputPort")
	@Test
	public void testMock() {
		assertTrue(createExecutionInputPort.createExecution("", null) == 1000, 
				() -> "ExecutionId should be 1000");
	}

}
