package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.Coverage;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;

@Component("ZurichAdapter")
@Scope("prototype")
@Log4j2
public class ZurichAdapter implements RobotOutputPort {

	private String baseUrl;
	private Map<String, String> sessionCookies;
	private String username;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		
		baseUrl = "https://infoweb.zurichspain.com/mizurich";
		
		username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		Response loginResponse = given().
			header("Content-Type", "application/x-www-form-urlencoded").
			formParam("versionApp", "4.9.20").
			formParam("langApp", "en").
			formParam("j_password", password).
			formParam("model", "SM-J710F").
			formParam("rememberMe", "0").
			formParam("j_username", username).
			formParam("versionOS", "27").
			formParam("device", "j7xelte").
			formParam("notifications", "1").
			formParam("token", "").
			log().all().
		when().
			post(baseUrl + "/j_security_check").
		then().
			log().all().
		extract().
		response();
		
		sessionCookies = loginResponse.cookies();
		
		boolean logged = false;
		if (loginResponse.statusCode() == 302) {
			String location = loginResponse.getHeader("Location");
			if (location.contains("login_success")) {
				
				Response loginResponse2 = given().
					param("login_success", "true").
					cookies(sessionCookies).
					log().all().
				when().
					get(baseUrl + "/app/sec/login").
				then().
					//log().all().
				extract().
				response();
				
				
				logged = true;
			}
		}
		else {
			throw new GenericAggregationException(loginResponse.asString());
		}
		return logged;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			cookies(sessionCookies).
			log().all().
		when().
			get(baseUrl + "/app/misdatos").
		then().
			//log().all().
		extract().
		response();
		
		Document doc = Jsoup.parseBodyFragment(response.asString());
		
		String holderName = doc.select(".usernameText").text();
		String email = doc.select("input[id*=inputTextMail]").val();
		String phoneNumber = doc.select("input[id*=nputTextPhone]").val();
		
		personalInformation.setHolderName(holderName);
		personalInformation.setEmailAddress(email);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setNif(username);
		
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		
		Response response = given().
			cookies(sessionCookies).
			log().all().
		when().
			get(baseUrl + "/app/misPolizas_flow").
		then().
			//log().all().
		extract().
		response();
		
		Document doc = Jsoup.parseBodyFragment(response.asString());
		
		doc.select(".principalBoxList .boxP").
			stream().
			filter(i -> i.text().contains("Seguro del hogar")).
			forEach(i -> {
				var insurance = new HomeInsurance();
				insurances.add(insurance);
			});
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		Response response = given().
			cookies(sessionCookies).
			
			log().all().
		when().
			get(baseUrl + "/app/misPolizas_flow").
		then().
			//log().all().
		extract().
		response();
		
		
		Document doc = Jsoup.parseBodyFragment(response.asString());
		String flowId = doc.select("input[type=hidden][id*=ViewState]").val();
		String formSignature = doc.select("input[type=hidden][name*=FormSignature]").val();
		
		log.info("FlowId:" + flowId);
		doc.select(".principalBoxList .boxP").
			stream().
			filter(i -> i.text().contains("Seguro de coche")).
			forEach(i -> {
				var insurance = new CarInsurance();
				
				String policyNumber = i.select(".policyNumber").text().
						replaceAll("[^\\d]", "");
				String policyName = "Seguro de coche";
				String brandAndModel = i.select(".title").text();
				
				
				/////Recibos//////
				String recibosBody = "misPolizasForm=misPolizasForm&javax.faces.ViewState=" + flowId + 
						"&javax.faces.FormSignature=" + formSignature + "&misPolizasForm%3ApblActivePoliciesHiddenRow=0&javax.faces.source=misPolizasForm%3Aj_id12&javax.faces.partial.execute=misPolizasForm%3Aj_id12%20%40component&javax.faces.partial.render=%40component&org.richfaces.ajax.component=misPolizasForm%3Aj_id12&misPolizasForm%3Aj_id12=misPolizasForm%3Aj_id12&AJAX%3AEVENTS_COUNT=1&javax.faces.partial.ajax=true";
				
				Response recibosResponse = given().
					contentType(ContentType.URLENC).
					header("Faces-Request", "partial/ajax").
					cookies(sessionCookies).
					body(recibosBody).
					log().all().
				when().
					post(baseUrl + "/app/misPolizas_flow?execution={flowId}", flowId).
				then().
					log().all().
				extract().
				response();
				
				String recibosRedirectUrl = StringUtils.
						substringBetween(recibosResponse.asString(), "url=\"", "\"");
				log.info(recibosRedirectUrl);
				
				
				Response recibosResponse2 = given().
					cookies(sessionCookies).
					param("execution", StringUtils.substringAfter(recibosRedirectUrl, "=")).
					log().all().
				when().
					get(baseUrl + "/app/misPolizas_flow").
				then().
					//log().all().
				extract().
				response();
				
				Document recibosDoc = Jsoup.parseBodyFragment(recibosResponse2.asString());
				recibosDoc.select(".attributesBox").
					stream().
					limit(1).
					forEach(r -> {
						String recurrence = r.select(".attributesBoxSecondBoxText .attributesBoxItem:nth-of-type(4)").text();
						String premium = r.select(".attributesBoxSecondBoxText .attributesBoxItem:nth-of-type(5)").text().
								replaceAll("[^\\d,]", "").replace(",", ".");
						String dueTo = r.select(".attributesBoxAndText .attributesBoxItem:nth-of-type(2)").text().
								replaceAll("[^\\d/]", "");
						insurance.setRecurrence(recurrence);
						insurance.setPremium(Double.parseDouble(premium));
						insurance.setDueDate(LocalDate.parse(dueTo, this.getDefaultDateFormatter2()));
					});
					
				///////////////Datos del conductor
				
				String driverBody = "misPolizasForm=misPolizasForm&javax.faces.ViewState=" + flowId + 
						"&javax.faces.FormSignature=" + formSignature + "&misPolizasForm%3ApblActivePoliciesHiddenRow=0&javax.faces.source=misPolizasForm%3Aj_id13&javax.faces.partial.execute=misPolizasForm%3Aj_id13%20%40component&javax.faces.partial.render=%40component&org.richfaces.ajax.component=misPolizasForm%3Aj_id13&misPolizasForm%3Aj_id13=misPolizasForm%3Aj_id13&AJAX%3AEVENTS_COUNT=1&javax.faces.partial.ajax=true";
				
				Response driverResponse = given().
					contentType(ContentType.URLENC).
					header("Faces-Request", "partial/ajax").
					cookies(sessionCookies).
					body(driverBody).
					log().all().
				when().
					post(baseUrl + "/app/misPolizas_flow?execution={flowId}", flowId).
				then().
					log().all().
				extract().
				response();
				
				String driverRedirectUrl = StringUtils.
						substringBetween(driverResponse.asString(), "url=\"", "\"");
				log.info(recibosRedirectUrl);
				
				Response driverResponse2 = given().
					cookies(sessionCookies).
					param("execution", StringUtils.substringAfter(driverRedirectUrl, "=")).
					log().all().
				when().
					get(baseUrl + "/app/misPolizas_flow").
				then().
					//log().all().
				extract().
				response();
				
				Document driverDoc = Jsoup.parseBodyFragment(driverResponse2.asString());
				driverDoc.select("div[id*=ppInsuredObj] .lista").
					stream().
					filter(d -> d.text().contains("Modelo:")).
					forEach(d -> {
						String model = d.select("div:nth-of-type(2)").text();
						insurance.setModel(model);
					});
				
				driverDoc.select("div[id*=ppInsuredObj] .lista").
					stream().
					filter(d -> d.text().contains("Marca:")).
					forEach(d -> {
						String brand = d.select("div:nth-of-type(2)").text();
						insurance.setBrand(brand);
					});
				
				driverDoc.select("div[id*=ppInsuredObj] .lista").
					stream().
					filter(d -> d.text().contains("Versión:")).
					forEach(d -> {
						String extendedModelInfo = d.select("div:nth-of-type(2)").text();
						insurance.setExtendedModelInfo(extendedModelInfo);
					});
				
				driverDoc.select("div[id*=ppInsuredObj] .lista").
					stream().
					filter(d -> d.text().contains("Matrícula:")).
					forEach(d -> {
						String cardPlate = d.select("div:nth-of-type(2)").text();
						insurance.setCarPlate(cardPlate);
					});
				
				driverDoc.select("div[id*=ppInsuredObj] .lista").
					stream().
					filter(d -> d.text().contains("Tipo de vehículo:")).
					forEach(d -> {
						String vehicleType = d.select("div:nth-of-type(2)").text();
						insurance.setVehicleType(vehicleType);
					});
				
				driverDoc.select("div[id*=ppInsuredParty] .lista").
					stream().
					filter(d -> d.text().contains("Nombre y apellidos")).
					forEach(d -> {
						String mainDriverName = d.select("div:nth-of-type(2)").text();
						insurance.setMainDriverName(mainDriverName);
						insurance.setMainDriverNif(username);
					});
				
				driverDoc.select("div[id*=ppInsuredParty] .lista").
					stream().
					filter(d -> d.text().contains("Fecha de nacimiento")).
					forEach(d -> {
						String birthdateDate = d.select("div:nth-of-type(2)").text();
						insurance.setMainDriverBirthDate(LocalDate.parse(birthdateDate, this.getDefaultDateFormatter2()));
					});
				
				driverDoc.select("div[id*=ppInsuredParty] .lista").
					stream().
					filter(d -> d.text().contains("Fecha de carné")).
					forEach(d -> {
						String carnetDate = d.select("div:nth-of-type(2)").text();
						Integer carnetYears = LocalDate.now().getYear() - LocalDate.parse(carnetDate, this.getDefaultDateFormatter2()).getYear();
						insurance.setMainDriverAgeOfCarnet(carnetYears);
					});
				
				driverDoc.select("div[id*=ppInsuredParty] .lista").
					stream().
					filter(d -> d.text().contains("Cuenta bancaria")).
					forEach(d -> {
						String iban = d.select("div:nth-of-type(2)").text();
						insurance.setIban(iban);
					});
				
				List<Coverage> coverages = driverDoc.select("div[id*=ppContractedGuaranties] .textContainer").
					stream().
					map(d -> {
						var coverage = new Coverage();
						coverage.setName(d.text());
						return coverage;
					}).collect(Collectors.toList());
				
				
				insurance.setCoverages(coverages);
				insurance.setProductName(policyName);
				insurance.setProductId(policyNumber);
				insurances.add(insurance);
			});
		
		return insurances;
	}

}
