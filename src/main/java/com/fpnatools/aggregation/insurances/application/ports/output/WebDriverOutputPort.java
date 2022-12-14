package com.fpnatools.aggregation.insurances.application.ports.output;

import org.openqa.selenium.WebDriver;

public interface WebDriverOutputPort {

	public WebDriver getInstance(String endpoint);
	public void takesScreenshot(String entity, WebDriver webDriver);
	public String getHtmlGet(WebDriver driver, String endpoint);
	public String getHtmlPost(WebDriver driver, String endpoint, String body);
}
