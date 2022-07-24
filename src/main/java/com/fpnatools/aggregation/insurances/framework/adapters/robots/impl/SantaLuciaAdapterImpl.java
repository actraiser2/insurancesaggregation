package com.fpnatools.aggregation.insurances.framework.adapters.robots.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.framework.adapters.RobotAdapter;
import com.fpnatools.aggregation.insurances.framework.adapters.WebDriverAdapter;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CarInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.PersonalInformationDTO;

@Component("SantaLuciaAdapter")
@Scope("prototype")
public class SantaLuciaAdapterImpl implements RobotAdapter {

	@Autowired WebDriverAdapter webDriverAdapter;
	private WebDriver webDriver;
	
	private Logger logger = LoggerFactory.getLogger(SantaLuciaAdapterImpl.class);
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		webDriver = webDriverAdapter.getInstance("https://clientes.santalucia.es/");
		webDriver.manage().window().setSize(new Dimension(1256, 1000));
		
		String  username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		webDriver.findElement(By.id("onetrust-accept-btn-handler")).click();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
		
		
		webDriver.findElement(By.id("username")).sendKeys(username);
		webDriver.findElement(By.id("password")).sendKeys(password);
		
		webDriver.findElement(By.id("submit")).click();
		
		boolean logged = webDriver.findElements(By.cssSelector("li.logout")).size() > 0;
		if (logged) {
			return true;
		}
		else {
			if (!webDriver.getPageSource().contains("Nombre de usuario o contraseña no válidos")) {
				throw new GenericAggregationException("Unknown error");
			}
		}
		
		webDriverAdapter.takesScreenshot("SantaLucia", webDriver);
		return false;
	}

	@Override
	public PersonalInformationDTO getPersonalInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HomeInsuranceDTO> getHomeInsurances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CarInsuranceDTO> getCarInsurances() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void releaseResources() {
		// TODO Auto-generated method stub
		try {
			logger.info("Closing WebDriver");
			webDriver.close();
			webDriver.quit();
		}
		catch(Exception ex) {
			logger.error("Error closing webdriver");
		}
	}

}
