package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.Coverage;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.Home;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("HelvetiaAdapter")
public class HelvetiaAdapter implements RobotOutputPort {

	private String baseUrl;
	private String ticket;
	private String username;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		baseUrl = "https://webservice.helvetia.es/rest-auth/b2c/v1";
		
		Response response = given().
			auth().preemptive().basic(username, password).
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			log().all().
		when().
			get(baseUrl + "/security/login").
		then().
			log().all().
		extract().
		response();
		
		if (response.statusCode() == 200) {
			ticket = response.jsonPath().getString("ticket");
			return true;
		}
		else if (response.statusCode() != 401) {
			throw new GenericAggregationException("");
		}
		
		return false;
		
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			header("Ticket", ticket).
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			log().all().
		when().
			get(baseUrl + "/clientes/{username}-000", username).
		then().
			log().all().
		extract().
		response();
		
		JsonPath jsonPath = response.jsonPath();
		
		String holderName = jsonPath.getString("nombre") + " " + 
				jsonPath.getString("apellido1") + " " + jsonPath.getString("apellido2");
		String nif = jsonPath.getString("identificador");
		String email = jsonPath.getString("email");
		String phoneNumber = jsonPath.getString("telefono");
		String birthDate = jsonPath.getString("fechaNacimiento");
		
		personalInformation.setHolderName(holderName);
		personalInformation.setNif(nif);
		personalInformation.setEmailAddress(email);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setBirthDate(LocalDate.parse(birthDate, this.getDefaultDateFormatter2()));
		
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		
		var insurances = new ArrayList<HomeInsurance>();
		Response response = given().
			header("Ticket", ticket).
			header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
			log().all().
		when().
			get(baseUrl + "/clientes/{username}/polizas", username).
		then().
			log().all().
		extract().
		response();
		
		List<Map<String, Object>> insuranceList = response.jsonPath().getList("$");
		
		insuranceList.stream().
		filter(i -> {
			String producto = i.get("producto").toString();
			
			return producto.toLowerCase().contains("hogar");
		}).
		forEach(i -> {
			var insurance = new HomeInsurance();
			
			String productName = i.get("producto").toString();
			String productId = i.get("numeroPoliza").toString();
			String dueDate = i.get("fechaRenovacionExpiracion").toString();
			String rawHome = i.get("riesgoAsegurado").toString();
			
			insurance.setProductId(productId);
			insurance.setProductName(productName);
			insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter2()));
			
			var assuredHome = new Home();
			assuredHome.setRawAddress(rawHome);
			
			Response detailResponse = given().
				header("Ticket", ticket).
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
				log().all().
			when().
				get(baseUrl + "/clientes/{username}/polizas/{productId}", username, productId).
			then().
				log().all().
			extract().
			response();
			
			JsonPath detailJsonPath = detailResponse.jsonPath();
			
			String startingDate = detailJsonPath.getString("datosGenerales.fechaAlta");
			String recurrence = detailJsonPath.getString("datosGenerales.formaPago");
			String iban = detailJsonPath.getString("datosGenerales.iban");
			String capitalContenido = detailJsonPath.getString("datosLocalizaciones[0].datosRiesgoParticulares.capitalContenido").
					replaceAll("[^\\d,]", "").replace(",", ".");
			String capitalInmueble = detailJsonPath.getString("datosLocalizaciones[0].datosRiesgoParticulares.capitalInmueble").
					replaceAll("[^\\d,]", "").replace(",", ".");
			
			insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter2()));
			insurance.setRecurrence(recurrence);
			insurance.setAsseguredHome(assuredHome);
			insurance.setIban(iban);
			
			List<Map<String, Object>> coverageList = detailJsonPath.getList("datosLocalizaciones[0].grupoGarantias[0].listaGarantias");
			
			List<Coverage> coverages = coverageList.stream().
				map(c -> {
					String amount = c.get("capital").toString().replaceAll("[^\\d,]", "").replace(",", ".");
					String descripcion = c.get("descripcion").toString();
					
					Coverage coverage = new Coverage();
					coverage.setAmount(Double.parseDouble(amount));
					coverage.setName(descripcion);
					
					return coverage;
					
				}).
				collect(Collectors.toList());
			
			coverages.add(new Coverage("Capital Contenido", Double.parseDouble(capitalContenido)));
			coverages.add(new Coverage("Capital Inmueble", Double.parseDouble(capitalInmueble)));
			insurance.setCoverages(coverages);
			
			
			Response recibosResponse = given().
				header("Ticket", ticket).
				header("User-Agent", "Mozilla/5.0 (Linux; Android 8.1.0; SM-J710F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 Mobile Safari/537.36").
				log().all().
			when().
				get(baseUrl + "/clientes/{username}/recibos/", username).
			then().
				log().all().
			extract().
			response();
			
			List<Map<String, Object>> recibosList = recibosResponse.jsonPath().getList("$");
			
			recibosList.stream().filter(r -> r.get("numPoliza").toString().equals(productId)).
				findFirst().
				ifPresent(r -> {
					String premium = r.get("totalRecibo").toString();
					insurance.setPremium(Double.parseDouble(premium));
				});
			
			
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
