package com.fpnatools.aggregation.insurances.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompanyEntity;
import com.fpnatools.aggregation.insurances.domain.entity.UserEntity;
import com.fpnatools.aggregation.insurances.framework.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.repository.UserRepository;

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
				var mapfreEntity = new InsuranceCompanyEntity();
				mapfreEntity.setName("Mapfre");
				
				var lineaDirectaEntity = new InsuranceCompanyEntity();
				lineaDirectaEntity.setName("LineaDirecta");
				
				var mutuactivosEntity = new InsuranceCompanyEntity();
				mutuactivosEntity.setName("Mutuactivos");
				
				
				insuranceCompanyRepository.save(mapfreEntity);
				insuranceCompanyRepository.save(lineaDirectaEntity);
				insuranceCompanyRepository.save(mutuactivosEntity);
				
			}
			
			if (userRepository.count() == 0) {
				var userEntity = new UserEntity();
				userEntity.setAppUser("admin");
				userEntity.setPassword(passwordEncoder.encode("4dm1n"));
				userEntity.setRoleName("ADMIN");
				
				userRepository.save(userEntity);
			}
			
		};
	}
}
