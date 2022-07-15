package com.fpnatools.aggregation.insurances.framework.adapters.robots.impl;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

import io.reactivex.rxjava3.core.Observable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("AxaAdapter")
@Scope("prototype")
public class AxaAdapterImpl implements RobotAdapter {

	private Logger logger = LoggerFactory.getLogger(AxaAdapterImpl.class);
	private String baseUrl;
	private String accessToken;
	private String version;
	private String username;
	private JsonPath globalPositionJsonPath;
	private String holderName;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		username = credentials.get("username");
		String password = credentials.get("password");
		
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		baseUrl = "https://mw-myaxa.axa.es";
		version = "3.7.0";
		
		Response response = given().
			header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").
			header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
			header("mana-version", version).
			header("platform-version", "8.1.0").
			header("AXA-Application-Key", "myaxa_es").
			header("Accept-Language", "es").
			header("device", "android").
			header("time-zone", "GMT").
			header("User-Agent", "okhttp/3.12.2").
			header("Accept", "*/*").
			formParam("grant_type", "password").
			formParam("username", username.trim()).
			formParam("password", password).
			log().all().
		when().
			post(baseUrl + "/v3/api/oauth2/token/").
		then().
			log().all().
		extract().
		response();
		
		
		if (response.statusCode() == 200) {
			accessToken = response.jsonPath().getString("access_token");
			return true;
		}
		else if (response.statusCode() != 401) {
			throw new GenericAggregationException(response.asString());
		}
		return false;
	}

	@Override
	public PersonalInformationDTO getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformationDTO();
		
		String perfonalInformationBody = "{\r\n"
				+ "    \"operationName\": \"Profile\",\r\n"
				+ "    \"query\": \"query Profile { personalInformation { __typename customerAccountId fullName firstName lastName postalAddress { __typename value digitalService { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } } } emailAddress { __typename link value } landlinePhone { __typename link international value phoneType } mobilePhone customerInformation { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } } customerPreferences { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } titleKey subtitleKey } } settings { __typename changePasswordEnabled } }\",\r\n"
				+ "    \"variables\": {}\r\n"
				+ "}";
		
		
		Response response = given().
			header("Authorization", "Bearer " + accessToken).
			header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
			header("mana-version", version).
			header("platform-version", "8.1.0").
			header("AXA-Application-Key", "myaxa_es").
			header("Accept-Language", "es").
			header("device", "android").
			header("time-zone", "GMT").
			header("User-Agent", "okhttp/3.12.2").
			header("Content-Type", "application/json").
			header("Accept", "application/json").
			body(perfonalInformationBody).
			log().all().
		when().
			post(baseUrl + "/graphql").
		then().
			log().all().
		extract().
		response();
		
		JsonPath jsonPath = response.jsonPath();
		
		holderName = jsonPath.getString("data.personalInformation.fullName");
		String email = jsonPath.getString("data.personalInformation.emailAddress.value");
		String phoneNumber = jsonPath.getString("data.personalInformation.emailAddress.mobilePhone");
		String rawAddress = jsonPath.getString("data.personalInformation.postalAddress.value[0]");
		
		var home = new HomeDTO();
		home.setRawAddress(rawAddress);
		
		personalInformation.setHolderName(holderName);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setNif(username);
		personalInformation.setEmailAddress(email);
		personalInformation.setHome(home);
		
		return personalInformation;
	}

	@Override
	public List<HomeInsuranceDTO> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsuranceDTO>();
		
		String body = "{\r\n" + 
				"    \"operationName\": \"Policies\",\r\n" + 
				"    \"query\": \"query Policies { elegiblePolicies { __typename policyInfo { __typename typeName category formulaName highlightStatus details policyId statusName publicId productName mode description } allocationsSummary { __typename percentage } coverageSummary { __typename formula } documentsSummary { __typename date } notificationsSummary { __typename totalMessages unreadMessages } digitalServices { __typename claimDeclaration { __typename category highlighted incidentTypes { __typename code actionType listName } } actionType code statusCode suspendedMessage titleKey listName links { __typename rel uri method } } detailsAvailable } }\",\r\n" + 
				"    \"variables\": {}\r\n" + 
				"}";
		
		Response response = given().
			header("Authorization", "Bearer " + accessToken).
			header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
			header("mana-version", version).
			header("platform-version", "8.1.0").
			header("AXA-Application-Key", "myaxa_es").
			header("Accept-Language", "es").
			header("device", "android").
			header("time-zone", "GMT").
			header("User-Agent", "okhttp/3.12.2").
			header("Content-Type", "application/json").
			header("Accept", "application/json").
			body(body).
			//log().all().
		when().
			post(baseUrl + "/graphql").
		then().
			log().all().
		extract().
		response();
		globalPositionJsonPath = response.jsonPath();
		
		
		List<Map<String, Object>> insuranceList = response.jsonPath().getList("data.elegiblePolicies");
		
		if (insuranceList != null) {
			insuranceList.stream().
			filter(i -> ((Map)i.get("policyInfo")).get("category").equals("HOME")).
			forEach(i -> {
				Map<String, Object> infoMap = (Map)i.get("policyInfo");
				String insuranceName = infoMap.get("productName").toString();
				String insuranceId = infoMap.get("policyId").toString();
				String category = infoMap.get("category").toString();
				
				
				HomeInsuranceDTO insurance = new HomeInsuranceDTO();
				insurance.setProductId(insuranceId);
				insurance.setProductName(insuranceName);
				
				String detailBody = "{\r\n" + 
					"    \"operationName\": \"PolicyDetail\",\r\n" + 
					"    \"query\": \"query PolicyDetail($policyId: String!) { policyDetail(id: $policyId) { __typename message { __typename title description importanceLevel resourceType } resume { __typename annualAmount effectiveDate expirationDate vehicleModel vehicleMake vehicleRegistration vehicleBonusDescription vehicleBonusValue homeLocations { __typename homePolicyType homeContentsValue homeValue homeAddress homeOwnership } worksmenInsured worksmenActivity travelInsureds travelPolicyType travelCoveredAreas travelCoveredDestinations } policyInfo { __typename typeName category formulaName highlightStatus statusName policyId productName publicId details mode description } premiums { __typename advancePaymentAmount paymentMode premiumService { __typename code actionType statusCode suspendedMessage } periodicAmount renewalDate } generalCoverage { __typename formula effectiveDate expirationDate formula moreCoverages outstandingLoan taxQualification initialEffectiveDate } agent { __typename email fullName id photo phoneNumbers { __typename description descriptionLines phoneNumber } requestAppointment { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } url } } documents { __typename items { __typename id url issueDate description title } moreDocuments } stakeholders { __typename adherent mainDrivers mainInsureds secondaryDrivers secondaryInsureds policyHolders } clauses { __typename title descriptionLines } savings { __typename regularDeposit { __typename depositPlan nextDepositDate initialDepositAmount expectedPaymentAmount effectivePaymentAmount depositServices { __typename code actionType statusCode suspendedMessage } } regularWithdrawals { __typename withdrawalPlan nextWithdrawalPlanDate withdrawalServices { __typename code actionType statusCode suspendedMessage } } overallValues { __typename chartItems { __typename type amount value } netAmount netAmountDescription overallRisk { __typename type value fraction } performanceAmount performancePercentage { __typename type value formattedValue } availableAmount totalPaymentsAmount totalWithdrawalsAmount evalutionDate investmentStrategy interestRate singleVehicleAmount singleVehicleCount pawnedPolicy chartItems { __typename type amount value } } } } }\",\r\n" + 
					"    \"variables\": {\r\n" + 
					"        \"policyId\": \"" + insuranceId + "\"\r\n" + 
					"    }\r\n" + 
					"}";
				
				Response detailResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					body(detailBody).
					log().all().
				when().
					post(baseUrl + "/graphql").
				then().
					log().all().
				extract().
				response();
				
				JsonPath jsonPathDetail = detailResponse.jsonPath();
				
				String rawInsurancedHomeAddress = jsonPathDetail.getString("data.policyDetail.resume.homeLocations[0].homeAddress");
				String startingDate = jsonPathDetail.getString("data.policyDetail.resume.effectiveDate");
				String dueDate = jsonPathDetail.getString("data.policyDetail.resume.expirationDate");
				String premium = jsonPathDetail.getString("data.policyDetail.resume.annualAmount") != null ? 
						jsonPathDetail.getString("data.policyDetail.resume.annualAmount").replaceAll("[^\\d.]", "") : null;
				String documentId = detailResponse.jsonPath().getString("data.policyDetail.documents.items[0].id");
				
				HomeDTO asseguredHome = new HomeDTO();
				asseguredHome.setRawAddress(rawInsurancedHomeAddress);
				
				
				insurance.setAsseguredHome(asseguredHome);
				insurance.setPremium(premium != null ? Double.parseDouble(premium) : null);
				insurance.setStartingDate(startingDate != null ? LocalDate.parse(startingDate, this.getDefaultDateFormatter2()): null);
				insurance.setDueDate(dueDate != null ? LocalDate.parse(dueDate, this.getDefaultDateFormatter2()): null);
				
				
				String coveragesBody = "{\r\n"
						+ "    \"operationName\": \"Coverages\",\r\n"
						+ "    \"query\": \"query Coverages($policyId: String!) { coverageGroups(policyId: $policyId) { __typename coverages { __typename statusName statusCode name information summary { __typename effectiveDate expirationDate benefit} restrictions { __typename limit deductible franchise } beneficiaries { __typename name percentage } clauses customDetails { __typename title data { __typename key value } } } category } agent(policyId: $policyId) { __typename email fullName id photo phoneNumbers { __typename description descriptionLines phoneNumber } requestAppointment { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } url } } }\",\r\n"
						+ "    \"variables\": {\r\n"
						+ "        \"policyId\": \"" + insurance.getProductId() + "\"\r\n"
						+ "    }\r\n"
						+ "}";
				Response coveragesResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					body(coveragesBody).
					log().all().
				when().
					post(baseUrl + "/graphql").
				then().
					log().all().
				extract().
				response();
				
				List<Map<String, Object>> coverageList = coveragesResponse.jsonPath().getList("data.coverageGroups[0].coverages");
				
				List<CoverageDTO> coverages = coverageList.stream().map(c -> {
					String name = c.get("name").toString();
					String amount = ((List)c.get("information")).get(0).toString().replaceAll("[^\\d,]", "").replace(",", ".");
					return new CoverageDTO(name, Double.parseDouble(amount));
				}).
				collect(Collectors.toList());
				
				insurance.setCoverages(coverages);
				
				
				Response documentResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					log().all().
				when().
					get(baseUrl + "/v1/api/documents/download/" + documentId).
				then().
					//log().all().
				extract().
				response();
					
					
				try {
					PDDocument document = PDDocument.load(documentResponse.asByteArray());
					PDFTextStripper tStripper = new PDFTextStripper();
			        String pdfText = tStripper.getText(document);
			        //logger.warn(pdfText);
			        
			        String[] lines = pdfText.split("[\r\n]+");
			        
			       
			        
			        Observable.fromArray(lines).
			        	skipWhile(l -> !l.startsWith("Total a pagar:")).
			        	take(4).
			        	subscribe(l -> {
			        		if (l.contains("Total a pagar:")) {
			        			String _premium = StringUtils.substringAfter(l, ":").replaceAll("[^\\d.]",  "");
			        			insurance.setPremium(Double.parseDouble(_premium));
			        			
			        			//insurance.setDateOfBirth.);
			        		}
			        		
			        	});
			        
			 
				}
				catch(Exception ex) {
					logger.error("Error parsing pdf Axa:", ex);
				}

				
				insurances.add(insurance);
			});
		}
		
		
		
		return insurances;
	}

	@Override
	public List<CarInsuranceDTO> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsuranceDTO>();
		
		List<Map<String, Object>> insuranceList = globalPositionJsonPath.getList("data.elegiblePolicies");
		
		if (insuranceList != null) {
			insuranceList.stream().
			filter(i -> ((Map)i.get("policyInfo")).get("category").equals("AUTO")).
			forEach(i -> {
				Map<String, Object> infoMap = (Map)i.get("policyInfo");
				String insuranceName = infoMap.get("productName").toString();
				String insuranceId = infoMap.get("policyId").toString();
				String category = infoMap.get("category").toString();
				
				
				CarInsuranceDTO insurance = new CarInsuranceDTO();
				insurance.setProductId(insuranceId);
				insurance.setProductName(insuranceName);
				
				
				String detailBody = "{\r\n" + 
						"    \"operationName\": \"PolicyDetail\",\r\n" + 
						"    \"query\": \"query PolicyDetail($policyId: String!) { policyDetail(id: $policyId) { __typename message { __typename title description importanceLevel resourceType } resume { __typename annualAmount effectiveDate expirationDate vehicleModel vehicleMake vehicleRegistration vehicleBonusDescription vehicleBonusValue homeLocations { __typename homePolicyType homeContentsValue homeValue homeAddress homeOwnership } worksmenInsured worksmenActivity travelInsureds travelPolicyType travelCoveredAreas travelCoveredDestinations } policyInfo { __typename typeName category formulaName highlightStatus statusName policyId productName publicId details mode description } premiums { __typename advancePaymentAmount paymentMode premiumService { __typename code actionType statusCode suspendedMessage } periodicAmount renewalDate } generalCoverage { __typename formula effectiveDate expirationDate formula moreCoverages outstandingLoan taxQualification initialEffectiveDate } agent { __typename email fullName id photo phoneNumbers { __typename description descriptionLines phoneNumber } requestAppointment { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } url } } documents { __typename items { __typename id url issueDate description title } moreDocuments } stakeholders { __typename adherent mainDrivers mainInsureds secondaryDrivers secondaryInsureds policyHolders } clauses { __typename title descriptionLines } savings { __typename regularDeposit { __typename depositPlan nextDepositDate initialDepositAmount expectedPaymentAmount effectivePaymentAmount depositServices { __typename code actionType statusCode suspendedMessage } } regularWithdrawals { __typename withdrawalPlan nextWithdrawalPlanDate withdrawalServices { __typename code actionType statusCode suspendedMessage } } overallValues { __typename chartItems { __typename type amount value } netAmount netAmountDescription overallRisk { __typename type value fraction } performanceAmount performancePercentage { __typename type value formattedValue } availableAmount totalPaymentsAmount totalWithdrawalsAmount evalutionDate investmentStrategy interestRate singleVehicleAmount singleVehicleCount pawnedPolicy chartItems { __typename type amount value } } } } }\",\r\n" + 
						"    \"variables\": {\r\n" + 
						"        \"policyId\": \"" + insuranceId + "\"\r\n" + 
						"    }\r\n" + 
						"}";
				Response detailResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					body(detailBody).
					log().all().
				when().
					post(baseUrl + "/graphql").
				then().
					log().all().
				extract().
				response();
				
				JsonPath jsonPathDetail = detailResponse.jsonPath();
				
				String model = jsonPathDetail.getString("data.policyDetail.resume.vehicleModel");
				String brand = jsonPathDetail.getString("data.policyDetail.resume.vehicleMake");
				String startingDate = jsonPathDetail.getString("data.policyDetail.resume.effectiveDate");
				String dueDate = jsonPathDetail.getString("data.policyDetail.resume.expirationDate");
				String premium = jsonPathDetail.getString("data.policyDetail.resume.annualAmount") != null ? 
						jsonPathDetail.getString("data.policyDetail.resume.annualAmount").replaceAll("[^\\d.]", "") : null;
				String carPlate = jsonPathDetail.getString("data.policyDetail.resume.vehicleRegistration");
				String documentId = detailResponse.jsonPath().getString("data.policyDetail.documents.items[0].id");
				
				insurance.setPremium(premium != null ? Double.parseDouble(premium) : null);
				insurance.setStartingDate(startingDate != null ? LocalDate.parse(startingDate, this.getDefaultDateFormatter2()): null);
				insurance.setDueDate(dueDate != null ? LocalDate.parse(dueDate, this.getDefaultDateFormatter2()): null);
			
				insurance.setMainDriverNif(username);
				insurance.setMainDriverName(holderName);
				insurance.setCarPlate(carPlate);
				insurance.setModel(model);
				insurance.setBrand(brand);
				
				
				
				
				
				Response documentResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					log().all().
				when().
					get(baseUrl + "/v1/api/documents/download/" + documentId).
				then().
					//log().all().
				extract().
				response();
				
				
				try {
					PDDocument document = PDDocument.load(documentResponse.asByteArray());
					PDFTextStripper tStripper = new PDFTextStripper();
			        String pdfText = tStripper.getText(document);
			        //logger.warn(pdfText);
			        
			        String[] lines = pdfText.split("[\r\n]+");
			        
			       
			        
			        Observable.fromArray(lines).
			        	skipWhile(l -> !l.startsWith("Conductor")).
			        	take(32).
			        	subscribe(l -> {
			        		if (l.contains("nacimiento")) {
			        			String birthdate = StringUtils.substringAfter(l, ":").trim();
			        			insurance.setMainDriverBirthDate(LocalDate.parse(birthdate, this.getDefaultDateFormatter2()));
			        			
			        			//insurance.setDateOfBirth.);
			        		}
			        		else if (l.contains("Fecha Permiso") ) {
			        			String mainDriverLicenseDate = StringUtils.substringAfter(l, ":").trim();
			        			insurance.setMainDriverAgeOfCarnet(LocalDate.now().getYear() - 
			        					LocalDate.parse(mainDriverLicenseDate, this.getDefaultDateFormatter2()).getYear());
			        			
			        			//insurance.setDateOfBirth.);
			        		}
			        		
			        	});
			        
			        Observable.fromArray(lines).
		        	skipWhile(l -> !l.startsWith("Precio Total")).
		        	take(5).
		        	subscribe(l -> {
		        		if (l.contains("Precio Total") && insurance.getPremium() == null) {
		        			String _premium = StringUtils.substringAfter(l, " ").replaceAll("[^\\d.]", "");
		        			insurance.setPremium(Double.parseDouble(_premium));
		        		}
		        		else if (l.contains("Forma de pago") ) {
		        			String paymentType = StringUtils.substringAfter(l, " ").trim();
		        			insurance.setRecurrence(paymentType);
		        			
		        			//insurance.setDateOfBirth.);
		        		}
		        		
		        	});
			
				}
				catch(Exception ex) {
					logger.error("Error parsing pdf Axa:", ex);
				}
				
				
				String coveragesBody = "{\r\n"
					+ "    \"operationName\": \"Coverages\",\r\n"
					+ "    \"query\": \"query Coverages($policyId: String!) { coverageGroups(policyId: $policyId) { __typename coverages { __typename statusName statusCode name information summary { __typename effectiveDate expirationDate benefit} restrictions { __typename limit deductible franchise } beneficiaries { __typename name percentage } clauses customDetails { __typename title data { __typename key value } } } category } agent(policyId: $policyId) { __typename email fullName id photo phoneNumbers { __typename description descriptionLines phoneNumber } requestAppointment { __typename code actionType statusCode suspendedMessage links { __typename uri rel method } url } } }\",\r\n"
					+ "    \"variables\": {\r\n"
					+ "        \"policyId\": \"" + insurance.getProductId() + "\"\r\n"
					+ "    }\r\n"
					+ "}";
			
				
				Response coveragesResponse = given().
					header("Authorization", "Bearer " + accessToken).
					header("x-axa-user-agent", "Android;MyAxa/3.3.3(com.axa.app.myaxa.es)").
					header("mana-version", version).
					header("platform-version", "8.1.0").
					header("AXA-Application-Key", "myaxa_es").
					header("Accept-Language", "es").
					header("device", "android").
					header("time-zone", "GMT").
					header("User-Agent", "okhttp/3.12.2").
					header("Content-Type", "application/json").
					header("Accept", "application/json").
					body(coveragesBody).
					log().all().
				when().
					post(baseUrl + "/graphql").
				then().
					log().all().
				extract().
				response();
					
				List<Map<String, Object>> coverageList = coveragesResponse.jsonPath().getList("data.coverageGroups[0].coverages");
				
				List<CoverageDTO> coverages = coverageList.stream().map(c -> {
					String name = c.get("name").toString();
					String amount = ((List)c.get("information")).get(0).toString().replaceAll("[^\\d,]", "").replace(",", ".");
					return new CoverageDTO(name, Double.parseDouble(amount));
				}).
				collect(Collectors.toList());
				
				insurance.setCoverages(coverages);
				
				insurances.add(insurance);
			});
		
		}
		
		
		return insurances;
	}

}
