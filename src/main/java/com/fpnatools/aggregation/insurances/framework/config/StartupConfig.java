package com.fpnatools.aggregation.insurances.framework.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fpnatools.aggregation.insurances.domain.entity.AppUser;
import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompany;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class StartupConfig {

	@Bean
	@Order(1)
	public CommandLineRunner startup(ApplicationContext context, Environment env, 
			InsuranceCompanyRepository insuranceCompanyRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			log.info("!!!!!! Initialing startup logic:" + env.getProperty("spring.application.name"));
			
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
				axaEntity.setName("Axa");
				
				var caserEntity = new InsuranceCompany();
				caserEntity.setName("Caser");
				
				var santaLuciaEntity = new InsuranceCompany();
				santaLuciaEntity.setName("Santa Lucia");
				
				
				insuranceCompanyRepository.save(mapfreEntity);
				insuranceCompanyRepository.save(lineaDirectaEntity);
				insuranceCompanyRepository.save(mutuactivosEntity);
				insuranceCompanyRepository.save(allianzEntity);
				insuranceCompanyRepository.save(axaEntity);
				insuranceCompanyRepository.save(caserEntity);
				insuranceCompanyRepository.save(santaLuciaEntity);
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
	
	@Bean
	@Order(2)
	public CommandLineRunner startup2() {
		return args -> {
			log.info("!!!!!!!!! Initialing startup2 logic:");
		};
	}
	
	@EventListener(ContextRefreshedEvent.class)
	public void ContextRefreshedEvent(ContextRefreshedEvent event) {
		log.info(event.getSource() + " ContextRefreshedEvent");
		//throw new RuntimeException("");
	}
}
