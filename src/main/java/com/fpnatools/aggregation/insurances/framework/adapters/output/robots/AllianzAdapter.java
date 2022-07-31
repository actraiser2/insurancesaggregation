package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.vo.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.Coverage;
import com.fpnatools.aggregation.insurances.domain.vo.Home;
import com.fpnatools.aggregation.insurances.domain.vo.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.reactivex.rxjava3.core.Observable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;

@Component("AllianzAdapter")
@Scope("prototype")
@Log4j2
public class AllianzAdapter implements RobotOutputPort {

	private String baseUrl;
	private String drcw08SessionId;
	private String drmb02SessionId;
	private String drpc24SessionId;
	private String username;
	private JsonPath globalPositionJsonPath;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		baseUrl = "https://seguros.allianz.es";
		
		username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		Response response = given().
			header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").
			formParam("grant_type", "password").
			formParam("j_username", username.trim()).
			formParam("j_password", password).
			formParam("nocache", "Tue May 04 2021 10:11:29 GMT+0200 (Central European Summer Time)").
			formParam("p_request_id", "login_attempt").
			formParam("p_oe", "ALZ").
			formParam("p_version", "2.10.3").
			log().all().
		when().
			post(baseUrl + "/drmb02/j_security_check").
		then().
			log().all().
		extract().
		response();
		
		JsonPath jsonPath = response.jsonPath();
		if (response.statusCode() == 302) {
			
			drcw08SessionId = response.
				getDetailedCookies().
				asList().
				stream().
				filter(c -> c.getPath().contains("drcw08")).
				findFirst().
				get().
				getValue();
			
			drmb02SessionId = response.
				getDetailedCookies().
				asList().
				stream().
				filter(c -> c.getPath().contains("drmb02")).
				findFirst().
				get().
				getValue();
			
			drpc24SessionId = response.
				getDetailedCookies().
				asList().
				stream().
				filter(c -> c.getPath().contains("drpc24")).
				findFirst().
				get().
				getValue();
			
			
			given().
				cookie("JSESSIONID", drmb02SessionId).
				log().all().
			when().
				get(baseUrl + "/drmb02/jsp/loginOK.jsp").
			then().
				log().all().
			extract().
			response();
			
			
				
			return true;
		}
		else if (response.statusCode() == 200) {
			String errorCode = jsonPath.getString("codigoError");
			if (errorCode != null && errorCode.equals("001_00002")) {
				return false;
			}
			else {
				throw new GenericAggregationException(response.asString());
			}
		}
		else {
			throw new GenericAggregationException(response.asString());
		}
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			cookie("JSESSIONID", drmb02SessionId).
			param("p_request_id", "userData").
			param("p_oe", "ALZ").
			param("p_version", "2.10.3").
			//param("nocache", "Sat+Jul+09+2022+16%3A47%3A55+GMT%2B0200+(Central+European+Summer+Time)").
			log().all().
		when().
			get(baseUrl + "/drmb02/api/s/cliente/datos").
		then().
			log().all().
		extract().
		response();
		
		JsonPath jsonPath = response.jsonPath();
		
		String email = jsonPath.getString("respuesta.email");
		String holderName = jsonPath.getString("respuesta.nombre") + " " +
				jsonPath.getString("respuesta.apellido1") + " " + 
				jsonPath.getString("respuesta.apellido2");
		
		String phoneNumber = jsonPath.getString("respuesta.telefono1");
		
		personalInformation.setEmailAddress(email);
		personalInformation.setHolderName(holderName);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setNif(username);
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		Response response = given().
			cookie("JSESSIONID", drmb02SessionId).
			param("p_request_id", "userData").
			param("p_oe", "ALZ").
			param("p_version", "2.10.3").
			//param("nocache", "Sat+Jul+09+2022+16%3A47%3A55+GMT%2B0200+(Central+European+Summer+Time)").
			log().all().
		when().
			get(baseUrl + "/drmb02/api/s/cliente/polizas").
		then().
			log().all().
		extract().
		response();
		
		globalPositionJsonPath = response.jsonPath();
		
		List<Map<String, Object>> insuranceList = response.jsonPath().getList("respuesta");
		
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		if (insuranceList != null) {
			insuranceList.stream().
			filter(i -> i.get("ramo").toString().equals("2050")).
			forEach(i -> {
				String userId = i.get("usuario").toString();
				String productId = i.get("numero").toString();
				String productName = i.get("descripcion").toString();
				String startingDate = i.get("fechaInicio").toString();
				String dueDate = i.get("fechaRenovacion").toString();
				String urlPdf = i.get("urlPdf").toString();
		
				Response pdfResponse = given().
					cookie("JSESSIONID", this.drcw08SessionId).
					urlEncodingEnabled(false).
					//param("nocache", "Sat+Jul+09+2022+16%3A47%3A55+GMT%2B0200+(Central+European+Summer+Time)").
					log().all().
				when().
					get(baseUrl + urlPdf).
				then().
					//log().all().
				extract().
				response();
				
				var insurance = new HomeInsurance();
				insurance.setProductName(productName);
				insurance.setProductId(productId);
				insurance.setStartingDate(LocalDate.parse(startingDate, dateFormatter));
				insurance.setDueDate(LocalDate.parse(dueDate, dateFormatter));
				
				
				try {
					PDDocument document = PDDocument.load(pdfResponse.asByteArray());
					PDFTextStripper tStripper = new PDFTextStripper();
			
					String pdfText = tStripper.getText(document);
					String[] linesPdf = pdfText.split("[\r\n]+");
					
					Observable.fromArray(linesPdf).
						skipWhile(l -> !l.contains("Tipo de Vivienda")).
						//doOnNext(l -> logger.info(l)).
						take(30).
						subscribe(l -> {
							if (l.contains("Tipo de Vivienda:")) {
								String houseType = StringUtils.substringAfter(l, ":").trim();
								insurance.setHouseType(houseType);
							}
							else if (l.contains("Dirección:")) {
								String address = StringUtils.substringAfter(l, ":").trim();
								Home asseguredHome = new Home();
								asseguredHome.setRawAddress(address);
								insurance.setAsseguredHome(asseguredHome);
							}
							else if (l.contains("Año Construcción:")) {
								String constructionYear = StringUtils.substringAfter(l, ":").trim();
								insurance.setConstructionYear(constructionYear);
							}
							else if (l.contains("Superficie Vivienda:")) {
								String squaredMetres = StringUtils.substringAfter(l, ":").trim();
								insurance.setSquaredMetres(squaredMetres);
							}
						});
					
					
					List<Coverage> coverages = Observable.fromArray(linesPdf).
						skipWhile(l -> !l.contains("Bien Asegurado")).
						skip(1).
						//doOnNext(l -> logger.info(l)).
						takeWhile(l -> NumberUtils.isParsable(l.replaceAll("[^\\d,]", "").replace(",", "."))).
						take(10).
						map(l -> {
							var coverage = new Coverage();
							coverage.setName(l);
							coverage.setAmount(Double.parseDouble(l.replaceAll("[^\\d,]", "").replace(",", ".")));
							return coverage;
						}).toList().blockingGet();
					
					insurance.setCoverages(coverages);
						
					
					//logger.info(pdfText);
				} catch (IOException ex) {
					log.error("Error parsing pdf allianz:", ex);
				}
				
				
				Response documentsResponse = given().
					cookie("JSESSIONID", drmb02SessionId).
					param("p_poliza", productId).
					param("p_aplica", "0").
					param("p_doc", "doc_rcb").
					param("p_request_id", "Documento_RCB").
					param("p_oe", "ALZ").
					param("p_version", "2.10.3").
					//param("nocache", "Sat+Jul+09+2022+16%3A47%3A55+GMT%2B0200+(Central+European+Summer+Time)").
					log().all().
				when().
					get(baseUrl + "/drmb02/api/s/cliente/documentos").
				then().
					log().all().
				extract().
				response();
				
				String urlReceipt = documentsResponse.jsonPath().getString("respuesta.respuesta");
				
				if (StringUtils.isNotEmpty(urlReceipt)) {
					Response receiptResponse = given().
						cookie("JSESSIONID", this.drpc24SessionId).
						urlEncodingEnabled(false).
						//param("nocache", "Sat+Jul+09+2022+16%3A47%3A55+GMT%2B0200+(Central+European+Summer+Time)").
						log().all().
					when().
						get(baseUrl + urlReceipt).
					then().
						//log().all().
					extract().
					response();
					
					try {
						PDDocument document = PDDocument.load(receiptResponse.asByteArray());
						PDFTextStripper tStripper = new PDFTextStripper();
				
						String pdfText = tStripper.getText(document);
						String[] linesPdf = pdfText.split("[\r\n]+");
						
						Observable.fromArray(linesPdf).
							skipWhile(l -> !l.contains("Prima")).
							take(1).
							subscribe(l -> {
								String premium = l.replaceAll("[^\\d,]", "").replace(",", ".");
								insurance.setPremium(Double.parseDouble(premium));
							});
						
						Observable.fromArray(linesPdf).
							skipWhile(l -> !l.contains("Forma Pago")).
							skip(1).
							take(1).
							subscribe(l -> {
								insurance.setRecurrence(l);
							});
						
					}
					catch (IOException ex) {
						log.error("Error parsing pdf allianz:", ex);
					}
				}
				
				
				
				
				insurances.add(insurance);
			});
		}
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		return insurances;
	}

}
