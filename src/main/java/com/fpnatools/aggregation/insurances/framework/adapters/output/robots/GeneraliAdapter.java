package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.vo.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.Coverage;
import com.fpnatools.aggregation.insurances.domain.vo.Home;
import com.fpnatools.aggregation.insurances.domain.vo.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("GeneraliAdapter")
@Scope("prototype")
public class GeneraliAdapter implements RobotOutputPort {

	private String baseUrl;
	private String sessionToken;
	private JsonPath globalPositionJsonPath;
	private Home titularHome;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		String username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}

		baseUrl = "https://www.generali.es/dig_customerPortalWebServices/rest";
		
		String body = "{\"company\":\"K\",\"user\":\"" + username.trim() + "\",\"password\":\"" + password + "\"}";
		
		Response response = given().
			header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36").
			header("Content-Type", "application/json").
			header("x-vinshieldpublic", "vinshield").
			body(body).
			log().all().
		when().
			post(baseUrl + "/loginUserService").
		then().
			log().all().
		extract().
			response();
		
		if (response.statusCode() == 200) {
			JsonPath jsonPath = response.jsonPath();
			String responseCode = jsonPath.getString("digResponse.code");
			if (responseCode.equals("000")) {
				sessionToken = jsonPath.get("sesion");
				return true;
			}
			else if (!responseCode.equals("003")) {
				throw new GenericAggregationException("");
			}
		}
		
		else {
			throw new GenericAggregationException("");
		}
		
		return false;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36").
			header("Content-Type", "application/json").
			header("Authorization", "Bearer " + sessionToken).
			header("accept-language", "es-ES").
			body("{}").
			//log().all().
		when().
			post(baseUrl + "/basicDataUserService?Language=es").
		then().
			log().all().
		extract().
			response();
		
		JsonPath jsonPath = response.jsonPath();
		
		String holderName = jsonPath.getString("name") + " " + jsonPath.getString("surName1")
			+ " " + jsonPath.getString("surName2");
		String nif = jsonPath.getString("dni");
		String email = jsonPath.getString("email");
		String phoneNumber = jsonPath.getString("mobilePhone");
		titularHome = new Home();
		titularHome.setCity(jsonPath.getString("address.city"));
		titularHome.setPostalCode(jsonPath.getString("address.postalCode"));
		titularHome.setProvince(jsonPath.getString("address.province"));
		titularHome.setStreet(jsonPath.getString("address.street"));
		titularHome.setStreetType(jsonPath.getString("address.streetType"));
		titularHome.setNumber(jsonPath.getString("address.number"));
		
		personalInformation.setHolderName(holderName);
		personalInformation.setEmailAddress(email);
		personalInformation.setNif(nif);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setHome(titularHome);
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		
		Response response = given().
			header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36").
			header("Content-Type", "application/json").
			header("Authorization", "Bearer " + sessionToken).
			header("accept-language", "es-ES").
			body("{}").
			//log().all().
		when().
			post(baseUrl + "/policyListService?Language=es").
		then().
			log().all().
		extract().
			response();
		
		globalPositionJsonPath = response.jsonPath();
		
		List<Map<String, Object>> insuranceList = globalPositionJsonPath.getList("policyList");
		
		insuranceList.stream().
			filter(i -> i.get("type").equals("HOGAR")).
			forEach(i -> {
				var insurance = new HomeInsurance();
				
				String productName = i.get("productName").toString();
				String productId = ((Map)i.get("policy")).get("number").toString();
				String company = ((Map)i.get("policy")).get("company").toString();
				String branch = ((Map)i.get("policy")).get("branch").toString();
				String application = ((Map)i.get("policy")).get("application").toString();
				String rol = i.get("rol").toString();
				String scope = i.get("scope").toString();
				String center = i.get("center").toString();
				String adhesionCollective = i.get("adhesionCollective").toString();
				String rawAssuredHome = ((List)i.get("riskInsured")).get(0).toString();
				
				Home assuredHome = new Home();
				
				
				if (rawAssuredHome.contains(titularHome.getStreet())) {
					assuredHome = titularHome;
				}
				assuredHome.setRawAddress(rawAssuredHome);
				
				String detailBody = "{\"company\":\"" + company + "\",\"branch\":\"" + branch + 
						"\",\"number\":" + productId + ","
						+ "\"application\":" + application + ",\"rol\":\"" + rol + "\","
						+ "\"scope\":\"" + scope + "\","
						+ "\"center\":\"" + center + "\",\"adhesionCollective\":" + adhesionCollective + "}";
				
				Response detailResponse = given().
					header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36").
					header("Content-Type", "application/json").
					header("Authorization", "Bearer " + sessionToken).
					header("accept-language", "es-ES").
					body(detailBody).
					log().all().
				when().
					post(baseUrl + "/policyService?Language=es").
				then().
					log().all().
				extract().
					response();
				
				JsonPath detailJsonPath = detailResponse.jsonPath();
				
				String startingDate = detailJsonPath.getString("effectDate").substring(0, 10);
				String premium = detailJsonPath.getString("premium").replaceAll("[^\\d,-]", "").replace(",", ".");
				String recurrence = detailJsonPath.getString("paymentPeriodicity");
				String iban = detailJsonPath.getString("bankAccount.iban");
				String houseType = detailJsonPath.getString("house.propertyType");
				String insuredAmount = detailJsonPath.getString("house.insuredAmount").replaceAll("[^\\d,-]", "").replace(",", ".");
				
				insurance.setProductId(productId);
				insurance.setProductName(productName);
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter()));
				insurance.setIban(iban);
				insurance.setRecurrence(recurrence);
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setHouseType(houseType);
				insurance.setAsseguredHome(assuredHome);
				
				List<Map<String, Object>> coverageList = detailJsonPath.getList("house.insuredContents");
				
				List<Coverage> coverages = coverageList.stream().
					filter(c -> NumberUtils.isParsable(c.get("value").toString().replaceAll("[^\\d,]", "").replace(",", "."))).
					map(c -> {
						var coverageDTO = new Coverage();
						String name = c.get("key").toString();
						String value = c.get("value").toString().replaceAll("[^\\d,]", "").replace(",", ".");
						
						coverageDTO.setName(name);
						coverageDTO.setAmount(Double.parseDouble(value));
						return coverageDTO;
				}).collect(Collectors.toList());
				
				var insurancedContent = new Coverage();
				insurancedContent.setName("Cantidad asegurada");
				insurancedContent.setAmount(Double.parseDouble(insuredAmount));
				coverages.add(insurancedContent);
				
				insurance.setCoverages(coverages);
				insurances.add(insurance);
			});
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		List<Map<String, Object>> insuranceList = globalPositionJsonPath.getList("policyList");
		
		insuranceList.stream().
			filter(i -> i.get("type").equals("AUTOS")).
			forEach(i -> {
				var insurance = new CarInsurance();
				
				String productName = i.get("productName").toString();
				String productId = ((Map)i.get("policy")).get("number").toString();
				String company = ((Map)i.get("policy")).get("company").toString();
				String branch = ((Map)i.get("policy")).get("branch").toString();
				String application = ((Map)i.get("policy")).get("application").toString();
				String rol = i.get("rol").toString();
				String scope = i.get("scope").toString();
				String center = i.get("center").toString();
				String adhesionCollective = i.get("adhesionCollective").toString();
				
				String detailBody = "{\"company\":\"" + company + "\",\"branch\":\"" + branch + 
						"\",\"number\":" + productId + ","
						+ "\"application\":" + application + ",\"rol\":\"" + rol + "\","
						+ "\"scope\":\"" + scope + "\","
						+ "\"center\":\"" + center + "\",\"adhesionCollective\":" + adhesionCollective + "}";
				
				Response detailResponse = given().
					header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36").
					header("Content-Type", "application/json").
					header("Authorization", "Bearer " + sessionToken).
					header("accept-language", "es-ES").
					body(detailBody).
					//log().all().
				when().
					post(baseUrl + "/policyService?Language=es").
				then().
					//log().all().
				extract().
					response();
				
				JsonPath detailJsonPath = detailResponse.jsonPath();
				
				String startingDate = detailJsonPath.getString("effectDate").substring(0, 10);
				String premium = detailJsonPath.getString("premium").replaceAll("[^\\d,-]", "").replace(",", ".");;
				String brand = detailJsonPath.getString("vehicle.brand");
				String model = detailJsonPath.getString("vehicle.model");
				String carPlate = detailJsonPath.getString("vehicle.plateNumber");
				String recurrence = detailJsonPath.getString("paymentPeriodicity");
				String iban = detailJsonPath.getString("bankAccount.iban");
				
				List<Map<String, Object>> drivers = detailJsonPath.getList("drivers");
				drivers.
					stream().
					limit(1).
					forEach(c -> {
						String mainDriverNif = c.get("nif").toString();
						String mainDriverBirthDate = c.get("birthDate").toString().substring(0, 10);
						String mainDriverName = c.get("fullName").toString();
						String carnetDate = c.get("licenseCarIssueDate").toString().substring(0, 10);
						
						insurance.setMainDriverName(mainDriverName);
						insurance.setMainDriverNif(mainDriverNif);
						insurance.setMainDriverBirthDate(LocalDate.parse(mainDriverBirthDate, this.getDefaultDateFormatter()));
						insurance.setMainDriverAgeOfCarnet(LocalDate.now().getYear() - LocalDate.parse(carnetDate, this.getDefaultDateFormatter()).getYear());
						
					});
				
				insurance.setProductId(productId);
				insurance.setProductName(productName);
				insurance.setBrand(brand);
				insurance.setModel(model);
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setCarPlate(carPlate);
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter()));
				insurance.setIban(iban);
				insurance.setRecurrence(recurrence);
				
				List<Map<String, Object>> coverageList = detailJsonPath.getList("guarantees[0].guarantees");
				
				List<Coverage> coverages = coverageList.stream().map(c -> {
					var coverageDTO = new Coverage();
					String name = c.get("guarantee").toString();
					
					coverageDTO.setName(name);
					return coverageDTO;
				}).collect(Collectors.toList());
				
				insurance.setCoverages(coverages);
				
				insurances.add(insurance);
			});
		
		return insurances;
	}

}
