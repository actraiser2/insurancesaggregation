package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.application.ports.output.WebDriverOutputPort;
import com.fpnatools.aggregation.insurances.domain.vo.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

@Component("SantaLuciaAdapter")
@Scope("prototype")
public class SantaLuciaAdapter implements RobotOutputPort {

	@Autowired WebDriverOutputPort webDriverAdapter;
	private WebDriver webDriver;
	
	private Logger logger = LoggerFactory.getLogger(SantaLuciaAdapter.class);
	
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
		UUID.fromString("SSS");
		webDriverAdapter.takesScreenshot("SantaLucia", webDriver);
		return false;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
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
