package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.params.CookiePolicy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Component("PlusUltraAdapter")
@Scope("prototype")
@Slf4j
public class PlusUltraAdapter implements RobotOutputPort{
	
	private String baseUrl;
	private Map<String, String> sessionCookies;
	private Document globalPosition;

	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
	
		baseUrl = "https://clientes.plusultra.es";
		
		String username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		

		Response preloginResponse = given().
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			redirects().follow(false).
			log().all().
		when().
			get(baseUrl).
		then().
			log().all().
		extract().
			response();
		
		sessionCookies = preloginResponse.cookies();
		
		String bigServer = sessionCookies.get("BIGipServer~DMZ~bcn_apm_forms_ClientesGCO_https");
		
		Response preloginResponse2 = given().
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			log().all().
			cookies(sessionCookies).
		when().
			get(baseUrl + "/my.policy").
		then().
			log().headers().
		extract().
			response();
		
		
		sessionCookies = new HashMap<String, String>(preloginResponse2.cookies());
		sessionCookies.put("BIGipServer~DMZ~bcn_apm_forms_ClientesGCO_https", bigServer);
		
		//////
		
		Response loginResponse = given().
			formParam("curl", "Z2F").
			formParam("flags", "0").
			formParam("forcedownlevel", "0").
			formParam("formdir", "8").
			formParam("username", username + "_SPU").
			formParam("trusted", "0").
			formParam("usernameShow", username).
			formParam("password", password).
			formParam("vhost", "standard").
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			header("Accept-Language", "en-US").
			header("Sec-Fetch-Site", "same-origin").
			header("Sec-Fetch-Mode", "navigate").
			header("Referer", "https://clientes.plusultra.es/my.policy").
			header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").
			cookies(sessionCookies).
			log().all().
		when().
			post(baseUrl + "/my.policy").
		then().
			log().all().
		extract().
			response();
		
		sessionCookies = new HashMap<String, String>(loginResponse.cookies());
		sessionCookies.put("BIGipServer~DMZ~bcn_apm_forms_ClientesGCO_https", bigServer);
		
		boolean logged = false;
		if (loginResponse.statusCode() == 302) {
			
			Response postlogin2Response = given().
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
				redirects().follow(false).
				cookies(sessionCookies).
				log().all().
			when().
				get(baseUrl).
			then().
				log().all().
			extract().
				response();
			sessionCookies.put("dtCookie", postlogin2Response.cookie("postlogin2Response"));
			sessionCookies.put(" BIGipServer~DMZ~clientes.plusultra.es_80", postlogin2Response.cookie(" BIGipServer~DMZ~clientes.plusultra.es_80"));
			
			
			 RestAssuredConfig restAssuredConfig =
				        RestAssured.config()
				            .redirect(new RedirectConfig().followRedirects(true))
				            .httpClient(
				                new HttpClientConfig().setParam("http.protocol.cookie-policy", 
				                   CookiePolicy.BROWSER_COMPATIBILITY));
			
			Response postlogin3Response = given().config(restAssuredConfig).
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
				redirects().follow(true).
				cookies(sessionCookies).
				log().all().
			when().
				get(baseUrl + "/ARQ.ecliente.App.FE/Inicial/Default.aspx").
			then().
				//log().all().
			extract().
				response();
			
			globalPosition = Jsoup.parseBodyFragment(postlogin3Response.asString());
			
			
			logged = true;
		}
		else if (loginResponse.statusCode() != 200) {
			throw new GenericAggregationException("");
		}
		
		return logged;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		String holderName = globalPosition.select("#ctl00_NombreUsuario").text();
		
		personalInformation.setHolderName(holderName);
		
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		
		globalPosition.select("#ctl00_ContentPlaceHolder1_contenedorTablaResumen .module").
			stream().
			filter(i -> i.text().contains("Hogar")).
			flatMap(i -> i.select(".itemcontainer.row .inline-block").stream()).
			forEach(i -> {
				log.info(i.text());
			});
		
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		return null;
	}

}
