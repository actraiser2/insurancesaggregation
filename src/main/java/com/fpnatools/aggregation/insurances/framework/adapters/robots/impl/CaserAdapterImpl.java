package com.fpnatools.aggregation.insurances.framework.adapters.robots.impl;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.framework.adapters.RobotAdapter;
import com.fpnatools.aggregation.insurances.framework.adapters.WebDriverAdapter;
import com.fpnatools.aggregation.insurances.framework.adapters.webDriver.impl.WebDriverAdapterImpl;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CarInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CoverageDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.PersonalInformationDTO;
import com.google.common.io.Files;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("CaserAdapter")
@Scope("prototype")
public class CaserAdapterImpl implements RobotAdapter {

	@Autowired WebDriverAdapter webDriverAdapter;
	private String baseUrl;
	private WebDriver webDriver;
	private String username;
	
	private Logger logger = LoggerFactory.getLogger(CaserAdapterImpl.class);
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		baseUrl = "https://www.caser.es";
		
		String body = "{\r\n"
				+ "    \"cia\": \"C0031\",\r\n"
				+ "    \"claveApp\": \"CASER\",\r\n"
				+ "    \"claveEntidad\": \"C031\",\r\n"
				+ "    \"forzarStore\": \"false\",\r\n"
				+ "    \"idDispositivo\": \"f5d77839-70cc-49f7-a97f-10f00ada2d5c\",\r\n"
				+ "    \"idioma\": \"es\",\r\n"
				+ "    \"idiomaDispositivo\": \"en_US\",\r\n"
				+ "    \"nif\": \"" + username + "\",\r\n"
				+ "    \"notif\": \"1\",\r\n"
				+ "    \"password\": \"" + password + "\",\r\n"
				+ "    \"permisos\": \"NO\",\r\n"
				+ "    \"sistemaOperativo\": \"android\",\r\n"
				+ "    \"tokenPush\": \"\"\r\n"
				+ "}";
		
		Response loginResponse = given().
			contentType("application/json").
			log().all().
			body(body).
		when().
			post("https://saludmobile.caser.es/saludmobileback/login").
		then().
			log().all().
		extract().
			response();
		
		if (loginResponse.statusCode() == 200) {
			JsonPath jsonPath = loginResponse.jsonPath();
			String message = jsonPath.getString("message");
			if (message.equals("autentificar_usuario_login_ok")) {
				String superToken = jsonPath.getString("result.super_token");
				
					
				webDriver = webDriverAdapter.getInstance(baseUrl + "/acceso-app?n=" + username + "&t=" + superToken);
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				
				webDriver.findElement(By.id("consent_prompt_submit")).click();
				
				
				webDriverAdapter.takesScreenshot("Caser", webDriver);
				
				return true;
				
			}
			else if (message.equals("autentificar_usuario_login_usuario_no_existe_ko")) {
				return false;
			}
			else {
				throw new  GenericAggregationException(loginResponse.asString());
			}
		}
		
		else {
			throw new  GenericAggregationException(loginResponse.asString());
		}
	
	}

	@Override
	public PersonalInformationDTO getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformationDTO();
		
		String url = "https://www.caser.es/group/area-cliente/mi-perfil/datos-personales";
		String html = webDriverAdapter.getHtmlGet(webDriver, url);
		Document doc = Jsoup.parseBodyFragment(html);
		
		doc.select("div.control-group").
			stream().
			filter(d -> d.select("#E_NOMBRE_label").size() > 0).
			forEach(d -> {
				String holderName = d.select("span").text();
				personalInformation.setHolderName(holderName);
			});
		
		doc.select("div.control-group").
			stream().
			filter(d -> d.select("#E_EMAIL_label").size() > 0).
			forEach(d -> {
				String email = d.select("span").text();
				personalInformation.setEmailAddress(email);
			});
		
		doc.select("div.control-group").
			stream().
			filter(d -> d.select("#E_TELEFONO_MOVIL_label").size() > 0).
			forEach(d -> {
				String phoneNumber = d.select("span").text();
				personalInformation.setPhoneNumber(phoneNumber);
			});
		
		personalInformation.setNif(username);
		
		return personalInformation;
	}

	@Override
	public List<HomeInsuranceDTO> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsuranceDTO>();
		String url = "https://www.caser.es/group/area-cliente/mis-polizas";
		String html = webDriverAdapter.getHtmlGet(webDriver, url);
		
		
		
		Document doc = Jsoup.parseBodyFragment(html);
		
		String formularioUrl = doc.select("#formulario").attr("action");
		
		logger.info(formularioUrl);
		
		doc.select("div[id=S_SEGUROS_GENERALES_div]").
			stream().
			filter(i -> i.text().contains("CASER HOGAR")).
			forEach(i -> {
				var insurance = new HomeInsuranceDTO();
				
				String productName = i.select("#titulo").text();
				String productId = i.select("a[id*=ventana]").text();
				String radioId = i.select("#titulo a[onclick]").attr("onclick").
						replaceAll("[^\\d]", "");
				
				insurance.setProductId(productId);
				insurance.setProductName(productName);
				
				String detailBody = "S_SEGUROS_GENERALES_radio=" + radioId + "&detalleGeneral=detalle&indexPoliza=";
				
			
				
				String detailHtml = webDriverAdapter.getHtmlPost(webDriver, formularioUrl, detailBody);
				
				Document detailDoc = Jsoup.parseBodyFragment(detailHtml);
				
				String recurrence = detailDoc.select("#S_FORMA_PAGO").attr("value");
				String iban = detailDoc.select("#S_CUENTA_CARGO").attr("value");
				String rawAssuredHome = detailDoc.select("#S_DATOS_POLIZA").attr("value");
				String startingDate = detailDoc.select("#S_FECHA_EFECTO").attr("value");
				String dueDate = detailDoc.select("#S_FECHA_VENCIMIENTO").attr("value");
				
				HomeDTO assuredHome = new HomeDTO();
				assuredHome.setRawAddress(rawAssuredHome);
				
				List<CoverageDTO> coverages = detailDoc.select("#S_P_LISTACAP_LISTABEANS tbody tr").
					stream().
					map(c -> {
						var coverage = new CoverageDTO();
						String name = c.select("td:nth-of-type(1)").text();
						String amount = c.select("td:nth-of-type(2)").text().
								replaceAll("[^\\d,]", "").replace(",", ".");
						coverage.setName(name);
						coverage.setAmount(Double.parseDouble(amount));
						return coverage;
					}).collect(Collectors.toList());
				
				insurance.setCoverages(coverages);
				
				insurance.setRecurrence(recurrence);
				insurance.setIban(iban);
				insurance.setAsseguredHome(assuredHome);
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter2()));
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter2()));
				insurances.add(insurance);
			});
		return insurances;
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
