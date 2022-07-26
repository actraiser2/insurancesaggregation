package com.fpnatools.aggregation.insurances.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fpnatools.aggregation.insurances.domain.entity.AppUser;
import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompany;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

@Configuration
public class StartupConfig {

	private Logger logger = LoggerFactory.getLogger(StartupConfig.class);
	
	@Bean
	public CommandLineRunner startup(ApplicationContext context, Environment env, 
			InsuranceCompanyRepository insuranceCompanyRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			logger.info("Initialing startup logic:" + env.getProperty("spring.application.name"));
			
			if (insuranceCompanyRepository.count() == 0) {
				var mapfreEntity = new InsuranceCompany();
				mapfreEntity.setName("Mapfre");
				
				var lineaDirectaEntity = new InsuranceCompany();
				lineaDirectaEntity.setName("LineaDirecta");
				
				var mutuactivosEntity = new InsuranceCompany();
				mutuactivosEntity.setName("Mutuactivos");
				
				var allianzEntity = new InsuranceCompany();
				allianzEntity.setName("Allianz");
				
				var axaEntity = new InsuranceCompany();
				allianzEntity.setName("Axa");
				
				
				insuranceCompanyRepository.save(mapfreEntity);
				insuranceCompanyRepository.save(lineaDirectaEntity);
				insuranceCompanyRepository.save(mutuactivosEntity);
				insuranceCompanyRepository.save(allianzEntity);
				insuranceCompanyRepository.save(axaEntity);
				
			}
			
			if (userRepository.count() == 0) {
				var userEntity = new AppUser();
				userEntity.setAppUser("admin");
				userEntity.setPassword(passwordEncoder.encode("4dm1n"));
				userEntity.setRoleName("ADMIN");
				
				userRepository.save(userEntity);
			}
			
		};
	}
}
