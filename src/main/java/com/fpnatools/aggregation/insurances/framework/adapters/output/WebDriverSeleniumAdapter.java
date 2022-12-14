package com.fpnatools.aggregation.insurances.framework.adapters.output;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.WebDriverOutputPort;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class WebDriverSeleniumAdapter implements WebDriverOutputPort {

	private @Value("${selenium.grid.url}") String gridServerUrl;
	
	@Override
	public WebDriver getInstance(String endpoint) {
		// TODO Auto-generated method stub
		FirefoxOptions options = new FirefoxOptions();
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		options.setHeadless(true);
		log.info(endpoint);
		WebDriver driver = null;
		try {
			driver = new RemoteWebDriver(new URL(gridServerUrl), options);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//driver = new Augmenter().augment(driver);
		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30)).
			pageLoadTimeout(Duration.ofSeconds(30)).implicitlyWait(Duration.ofSeconds(8));
		
		driver.get(endpoint);
	
		
		return driver;
	}

	@Override
	public void takesScreenshot(String entity, WebDriver webDriver){
		// TODO Auto-generated method stub
		try {
			TakesScreenshot ts = (TakesScreenshot)webDriver;
			
			File sourceFile = ts.getScreenshotAs(OutputType.FILE);
			Path targetFile = Paths.get("/tmp", entity + ".png");
			Files.move(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(Exception ex) {
			log.error("Error taking screenshot:", ex);
		}
	}

	@Override
	public String getHtmlGet(WebDriver driver, String endpoint) {
		// TODO Auto-generated method stub
		JavascriptExecutor js = (JavascriptExecutor)driver;
		return js.executeAsyncScript("var callback = arguments[arguments.length -1];"
				+ "fetch(arguments[0]).then(r => r.text()).then(callback)", endpoint).
				toString();
		
	}
	
	@Override
	public String getHtmlPost(WebDriver driver, String endpoint, String body) {
		// TODO Auto-generated method stub
		JavascriptExecutor js = (JavascriptExecutor)driver;
		return js.executeAsyncScript("var callback = arguments[arguments.length -1];"
				+ "fetch(arguments[0], {'method':'POST', 'body': arguments[1],"
				+ "'headers':{'Content-Type':'application/x-www-form-urlencoded'}}).then(r => r.text()).then(callback)", 
				endpoint, body).toString();
		
	}
	

}
