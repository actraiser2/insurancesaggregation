package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi.SHA256;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.application.ports.output.WebDriverOutputPort;
import com.fpnatools.aggregation.insurances.domain.vo.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.Coverage;
import com.fpnatools.aggregation.insurances.domain.vo.Home;
import com.fpnatools.aggregation.insurances.domain.vo.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("SantaLuciaAdapter")
@Scope("prototype")
public class SantaLuciaAdapter implements RobotOutputPort {

	private Logger logger = LoggerFactory.getLogger(SantaLuciaAdapter.class);
	private String accessToken;
	private JsonPath globalPositionJsonPath;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		
		String codeVerifier = "KN3fF0nH2SqX3yPJb84M2XlRrow7TbTxBFBnMTmJivOhT62R1Bk7KBItwgc59CKvB1jgo4vY7SYy0SgEem8zePi5y5c9l3EZ";
		String challengeCode = Base64.encodeBase64URLSafeString(DigestUtils.sha256(codeVerifier.getBytes()));
		String clientId = "cac78633-cfb0-11ec-9fdc-331ad2553ff9";
		String redirectUri = "https://clientes.santalucia.es/";
		String preloginUrl = "https://sso-apis.santalucia.es/auth/realms/3scale/protocol/openid-connect/auth?client_id=" + clientId + "&redirect_uri=" + redirectUri + "&state=e96396ca-8a2e-40b6-81bb-84f9eacac53e&response_mode=fragment&response_type=code&scope=openid&nonce=6fc80087-8c6c-4340-aea1-a65d962adc25&code_challenge=" + challengeCode + "&code_challenge_method=S256";
	
		
		String  username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		
		Response preLoginresponse = given().
			log().all().
		when().
			get(preloginUrl).
		then().
			//log().all().
		extract().
		response();
		
		Document preLoginDoc = Jsoup.parse(preLoginresponse.asString());
		
		String loginUrl = preLoginDoc.select("#kc-form-login").attr("action");
		
		Response loginResponse = given().
			log().all().
			formParam("username", username).
			formParam("password", password).
			header("Content-Type", "application/x-www-form-urlencoded").
			header("accept-language", "es-ES,es;q=0.9").
			cookies(preLoginresponse.cookies()).
		when().
			post(loginUrl).
		then().
			log().all().
		extract().
		response();
		
		if (loginResponse.statusCode() == 302) {
			String location = loginResponse.getHeader("Location");
			String code = StringUtils.substringAfter(location, "code=");
			
			Response accessTokenResponse = given().
				log().all().
				formParam("code", code).
				formParam("grant_type", "authorization_code").
				formParam("client_id", clientId).
				formParam("redirect_uri", redirectUri).
				formParam("code_verifier", codeVerifier).
				header("Content-Type", "application/x-www-form-urlencoded").
				//cookies(preLoginresponse.cookies()).
			when().
				post("https://sso-apis.santalucia.es/auth/realms/3scale/protocol/openid-connect/token").
			then().
				log().all().
			extract().
			response();
			
			accessToken = accessTokenResponse.jsonPath().getString("access_token");
			return true;
			
		}
		else {
			if (!loginResponse.asString().contains("Nombre de usuario o contraseña no válidos")) {
				throw new GenericAggregationException("");
			}
		}
		
		
		
		return false;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			header("Authorization", "Bearer " + accessToken).
			header("x-request-id", UUID.randomUUID().toString()).
			contentType(ContentType.JSON).
			log().all().
			//cookies(preLoginresponse.cookies()).
		when().
			get("https://clientes.santalucia.es/api/apc/v1/perfiles").
		then().
			log().all().
		extract().
		response();
		
		JsonPath jsonPath = response.jsonPath();
		
		String email = jsonPath.getString("userInfo.correoElectronico");
		String phoneNumber = jsonPath.getString("userInfo.telefonoMovil");
		String holderName = jsonPath.getString("userInfo.nombre") + " " + 
				jsonPath.getString("userInfo.apellidos");
		String nif = jsonPath.getString("userInfo.documentoDeIdentificacion");
		String birthDate = jsonPath.getString("userInfo.fechaNacimiento");
		
		personalInformation.setEmailAddress(email);
		personalInformation.setNif(nif);
		personalInformation.setHolderName(holderName);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setBirthDate(LocalDate.parse(birthDate, this.getDefaultDateFormatter()));
		
		var titularHome = new Home();
		titularHome.setRawAddress(jsonPath.getString("userInfo.domicilio"));
		
		personalInformation.setHome(titularHome);
		
		
		
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		
		Response response = given().
			header("Authorization", "Bearer " + accessToken).
			header("x-request-id", UUID.randomUUID().toString()).
			contentType(ContentType.JSON).
			log().all().
			//cookies(preLoginresponse.cookies()).
		when().
			get("https://clientes.santalucia.es/api/apc/v1/polizas").
		then().
			log().all().
		extract().
		response();
		
		globalPositionJsonPath = response.jsonPath();
		
		List<Map<String, Object>> insuranceList = globalPositionJsonPath.getList("productos");
		
		insuranceList.stream().
			filter(i -> i.get("identificadorTipoPoliza").equals("6")).
			forEach(i -> {
				
				var insurance = new HomeInsurance();
				
				String productName = i.get("nombrePoliza").toString();
				String productId = i.get("identificadorPoliza").toString();
				String iban = i.get("iban").toString();
				String recurrence = i.get("modoPago").toString();
				String dueDate = i.get("fechaVencimiento").toString().substring(0, 10);
				String modalidad = i.get("identificadorModalidad").toString();
				
				insurance.setProductId(productId);
				insurance.setProductName(productName);
				insurance.setRecurrence(recurrence);
				insurance.setIban(iban);
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter()));
				
				var assuredHome = new Home();
				assuredHome.setRawAddress(i.get("direccion").toString());
				insurance.setAsseguredHome(assuredHome);
				
				Response detailResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-request-id", UUID.randomUUID().toString()).
					param("ramo", "6").
					param("modalidad", modalidad).
					param("primas", "true").
					contentType(ContentType.JSON).
					log().all().
					//cookies(preLoginresponse.cookies()).
				when().
					get("https://clientes.santalucia.es/api/apc/v1/polizas/" + productId).
				then().
					log().all().
				extract().
				response();
				JsonPath detailJsonPath = detailResponse.jsonPath();
				
				Response coveragesResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-request-id", UUID.randomUUID().toString()).
					param("ramo", "6").
					param("poliza", productId).
					contentType(ContentType.JSON).
					log().all().
					//cookies(preLoginresponse.cookies()).
				when().
					get("https://clientes.santalucia.es/api/apc/v1/siniestros-poliza").
				then().
					log().all().
				extract().
				response();
				JsonPath coveragesJsonPath = coveragesResponse.jsonPath();
				
				String startingDate = detailJsonPath.getString("infoPoliza.polizaInfo.fechaInicio");
				String premium = detailJsonPath.getString("resumen.primaAnual");
				
				List<Map<String, Object>> coverageList = coveragesJsonPath.getList("partidas");
				
				List<Coverage> coverages = coverageList.stream().
					map(c -> {

						String name = c.get("nombrePartida").toString();
						String value = c.get("capital").toString();
						
						return new Coverage(name, Double.parseDouble(value));
					}).
					toList();
				
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter()));
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setCoverages(coverages);
				insurances.add(insurance);
			});
		
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		return insurances;
	}
	

}
