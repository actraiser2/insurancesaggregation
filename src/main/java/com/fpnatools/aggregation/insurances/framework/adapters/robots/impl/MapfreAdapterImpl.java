package com.fpnatools.aggregation.insurances.framework.adapters.robots.impl;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.framework.adapters.RobotAdapter;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CarInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CoverageDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.PersonalInformationDTO;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("MapfreAdapter")
@Scope("prototype")
public class MapfreAdapterImpl implements RobotAdapter {

	private static Logger logger = LoggerFactory.getLogger(MapfreAdapterImpl.class);
	private String baseUrl;
	private String serviceVersion;
	private String securityToken;
	private JsonPath loginJsonPath;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		baseUrl = "https://webservices.mapfre.com/mmobile-core";
		serviceVersion = "12.4.12";
		
		String username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		Map<String, Object> loginParameters = new HashMap<>();
		loginParameters.put("username", StringUtils.leftPad(username, 10, "0").toUpperCase());
		loginParameters.put("password", StringUtils.substring(password, 0, 8));
		
		Response response = given().
			header("X-Version-Id", serviceVersion).
			header("MMOBILE_JWT_ACCEPT_TOKEN", "header").
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F Build/OPM1.171019.013; wv) AppleWebKit/537.36 "
					+ "(KHTML, like Gecko) Version/4.0 Chrome/79.0.3945.136 Mobile Safari/537.36").
			contentType("application/json").
			log().all().
			body(loginParameters).
		when().
			post(baseUrl + "/jwt/login/mets/" + serviceVersion + "/JWTOIM?" + 
					"roEntityCode=00&roGroupingCode=0&roOrigin=6&roProductId=Android&roOperationCode=0&roDocumentNumber=" + username).
		then().
			log().all().
		extract().
			response();
		
		boolean logged = false;
		securityToken = response.getHeader("MMOBILE_JWT_ACCESS_TOKEN"); 
		if (response.statusCode() == 200 && securityToken != null) {
			loginJsonPath = response.jsonPath();
	
			logged = true;
		}
		else {
			if (!response.asString().contains("INVALID_CREDENTIALS")) {
				throw new GenericAggregationException(response.asString());
			}
		}
		
		return logged;
	}
	
	

	@Override
	public PersonalInformationDTO getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformationDTO();
		
		String nif = loginJsonPath.getString("documentNumber");
		String birthDate = loginJsonPath.getString("birthdate");
		String holderName = loginJsonPath.getString("name") + " " + 
				loginJsonPath.getString("surname1") +  " " + loginJsonPath.getString("surname2");
		String email = loginJsonPath.getString("email");
		String phoneNumber = loginJsonPath.getString("phone");
		String street = loginJsonPath.getString("address.street");
		String city = loginJsonPath.getString("address.city");
		String postalCode = loginJsonPath.getString("address.postalCode");
		String province = loginJsonPath.getString("address.province");
		String number = loginJsonPath.getString("address.number");
		
		var home = new HomeDTO();
		home.setCity(city);
		home.setPostalCode(postalCode);
		home.setProvince(province);
		home.setNumber(number);
		home.setStreet(street);
		
		personalInformation.setHolderName(holderName);
		personalInformation.setNif(nif);
		personalInformation.setBirthDate(LocalDate.parse(birthDate, this.getDefaultDateFormatter()));
		personalInformation.setEmailAddress(email);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setHome(home);
		return personalInformation;
	}



	@Override
	public List<HomeInsuranceDTO> getHomeInsurances() {
		// TODO Auto-generated method stub
		var  insurances = new ArrayList<HomeInsuranceDTO>();
		
		Response response = given().
			header("X-Version-Id", serviceVersion).
			header("X-Authorization", "Bearer " + securityToken).
			param("ACICompaniesEnabled", true).
			contentType("application/json").
			log().all().
		when().
			get(baseUrl + "/services/j/mets/" + serviceVersion + "/middlewarePrivateOIMServices/"
					+ "homeOtherPolicies").
		then().
			log().all().
		extract().
			response();
		
		
		List<Map<String, Object>> insurancesList = response.jsonPath().getList("homePolicies");
		
		insurancesList.stream().forEach(i -> {
			var insurance = new HomeInsuranceDTO();
			
			String policyNumber = i.get("policyNumber").toString();
			String productName = i.get("productName").toString();
			String dueDate = i.get("expirationDate").toString();
			String rawAddress = ((Map)i.get("address")).get("rawAddress").toString();
			
			
			Response policyDetailResponse = given().
				header("X-Version-Id", serviceVersion).
				header("X-Authorization", "Bearer " + securityToken).
				param("type", "NSE").
				contentType("application/json").
				log().all().
			when().
				get(baseUrl + "/services/j/mets/" + serviceVersion + "/middlewarePrivateOIMServices/"
						+ "homeOtherPolicies/{policyId}", policyNumber).
			then().
				log().all().
			extract().
				response();
			
			JsonPath policyDetailJsonPath = policyDetailResponse.jsonPath();
			
			String iban = policyDetailJsonPath.getString("iban");
			String paymentMethod = policyDetailJsonPath.getString("paymentMethod");
			String premium = policyDetailJsonPath.getString("anualAmount");
			String startingDate = policyDetailJsonPath.getString("anualStartDate");
			String homeAdditionalDetail =  policyDetailJsonPath.getString("risk");
			List<Map<String, Object>> coverageList = policyDetailJsonPath.getList("coverages");
			
			List<CoverageDTO> coverages = coverageList.stream().map(c -> {
				var coverage = new CoverageDTO();
				String name = c.get("name").toString();
				String amount = c.get("amount") != null ? c.get("amount").toString() : "0";
				
				coverage.setName(name);
				coverage.setAmount(Double.parseDouble(amount) > 0 ? Double.parseDouble(amount) : null);
				return coverage;
			}).collect(Collectors.toList());
			
			insurance.setRawInsurancedHomeAddress(rawAddress);
			insurance.setProductId(policyNumber);
			insurance.setProductName(productName);
			insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter()));
			insurance.setIban(iban);
			insurance.setPremium(Double.parseDouble(premium));
			insurance.setStartingDate(startingDate != null ? LocalDate.parse(startingDate, this.getDefaultDateFormatter()): null);
			insurance.setRecurrence(paymentMethod);
			insurance.setHomeAdditionalDetail(homeAdditionalDetail);
			insurance.setCoverages(coverages);
			insurances.add(insurance);
		});
			
		
		return insurances;
	}



	@Override
	public List<CarInsuranceDTO> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsuranceDTO>();
		
		Response response = given().
			header("X-Version-Id", serviceVersion).
			header("X-Authorization", "Bearer " + securityToken).
			param("ACICompaniesEnabled", true).
			contentType("application/json").
			log().all().
		when().
			get(baseUrl + "/services/j/mets/" + serviceVersion + "/middlewarePrivateOIMServices/"
					+ "carPolicies").
		then().
			log().all().
		extract().
			response();
		
		List<Map<String, Object>> insurancesList = response.jsonPath().getList("carPolicies");
		
		insurancesList.stream().forEach(i -> {
			var insurance = new CarInsuranceDTO();
			
			String policyNumber = i.get("policyNumber").toString();
			String productName = i.get("product").toString();
			String carPlate = i.get("plateNumber").toString();
			
			Response policyDetailResponse = given().
				header("X-Authorization", "Bearer " + securityToken).
				header("X-Version-Id", serviceVersion).
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F Build/OPM1.171019.013; wv) AppleWebKit/537.36 "
						+ "(KHTML, like Gecko) Version/4.0 Chrome/79.0.3945.136 Mobile Safari/537.36").
				contentType("application/json").
				log().all().
			when().
				get(baseUrl + "/services/j/mets/" + serviceVersion + "/middlewarePrivateOIMServices/carPolicies/" + policyNumber).
			then().
				//log().all().
			extract().
				response();
			
			JsonPath policyDetailJsonPath = policyDetailResponse.jsonPath();
			
			String vehicle = policyDetailJsonPath.getString("vehicle");
			String paymentMethod = policyDetailJsonPath.getString("paymentMethod");
			String iban = policyDetailJsonPath.getString("iban");
			String dueDate = policyDetailJsonPath.getString("expirationDate");
			String mainDriverName = null;
			String mainDriverNif = null;
			String mainDriverBirthDate = null;
			String mainDriverCarnetDate = null;
			Integer antigCarnet = 0;
			
			Boolean mainDriver = policyDetailJsonPath.getBoolean("forms[0].habitualDriver");
			if (BooleanUtils.isTrue(mainDriver)) {
				mainDriverName = policyDetailJsonPath.getString("forms[0].name") + " " + policyDetailJsonPath.getString("forms[0].surname1") 
					+ " " + policyDetailJsonPath.getString("forms[0].surname2");
				
				mainDriverNif = policyDetailJsonPath.getString("forms[0].documentNumber");
				mainDriverBirthDate = policyDetailJsonPath.getString("forms[0].birthdate");
				mainDriverCarnetDate = policyDetailJsonPath.getString("forms[0].permissionDate");
				antigCarnet = LocalDate.now().getYear() - LocalDate.parse(mainDriverCarnetDate, this.getDefaultDateFormatter()).getYear();
			}
			
			List<String> coverageList = policyDetailJsonPath.getList("coverages");
			
			List<CoverageDTO> coverages = coverageList.stream().map(c -> {
				var coverage = new CoverageDTO();
				coverage.setName(c);
				return coverage;
			}).collect(Collectors.toList());
			
			
			Response receiptsResponse = given().
				header("X-Authorization", "Bearer " + securityToken).
				header("X-Version-Id", serviceVersion).
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F Build/OPM1.171019.013; wv) AppleWebKit/537.36 "
						+ "(KHTML, like Gecko) Version/4.0 Chrome/79.0.3945.136 Mobile Safari/537.36").
				contentType("application/json").
				log().all().
			when().
				get(baseUrl + "/services/j/mets/" + serviceVersion + "/middlewarePrivateOIMServices/carPolicies/" + policyNumber + 
						"/receipts?companyId=" + i.get("companyId").toString()).
			then().
				//log().all().
			extract().
				response();
			List<Map<String, Object>> receiptList = receiptsResponse.jsonPath().getList("$");
			
			if (receiptList != null) {
				receiptList.stream().limit(1).
					forEach(r -> {
						insurance.setPremium(Double.parseDouble(r.get("amount").toString()));
					});
			}
			
			insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter()));
			insurance.setProductId(policyNumber);
			insurance.setProductName(productName);
			insurance.setCarPlate(carPlate);
			insurance.setIban(iban);
			insurance.setRecurrence(paymentMethod);
			insurance.setModel(vehicle);
			insurance.setMainDriverName(mainDriverName);
			insurance.setMainDriverNif(mainDriverNif);
			insurance.setMainDriverBirthDate(LocalDate.parse(mainDriverBirthDate, this.getDefaultDateFormatter()));
			insurance.setMainDriverAgeOfCarnet(antigCarnet);
			insurance.setCoverages(coverages);
			insurances.add(insurance);
		});
		
		return insurances;
	}
	
	

}
