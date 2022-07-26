package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.domain.vo.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.Coverage;
import com.fpnatools.aggregation.insurances.domain.vo.Home;
import com.fpnatools.aggregation.insurances.domain.vo.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.vo.PersonalInformation;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Component("MutuactivosAdapter")
@Scope("prototype")
public class MutuactivosAdapter implements RobotOutputPort {

	private String baseUrl;
	private String authToken;
	private String username;
	private JsonPath globalPositionJsonPath;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		baseUrl = "https://api.mutua.es/produccion/canalcliente";
		
		username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}
		
		Response response = given().
			contentType(ContentType.URLENC).
			header("x-mutua-feventsource", "acceder").
			header("User-Agent", ":	okhttp/4.8.1").
			formParam("password", password).
			formParam("username", username).
			formParam("grant_type", "password").
			formParam("scope", "operate").
			formParam("X-mutua-channel", "APP-MM").
			formParam("client_id", "20b04c60-d8a2-439d-bc3f-d4f4f1e0fa67").
			formParam("canal", "MOV").
			relaxedHTTPSValidation().
			log().all().
		when().
			post("https://ssodigital.mutua.es/produccion/canalcliente/oauth2/token").
		then().
			log().all().
		extract().
			response();
		
		if (response.statusCode() == 200) {
			authToken = response.jsonPath().getString("access_token");
			return true;
		}
		else {
			if (response.statusCode() != 401) {
				throw new GenericAggregationException(response.asString());
			}
		}
		return false;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		var personalInformation = new PersonalInformation();
		
		Response response = given().
			header("x-mutua-feventsource", "acceder").
			header("User-Agent", ":	okhttp/4.8.1").
			header("Authorization", "Bearer " + authToken).
			header("User-Agent", ":	okhttp/4.8.1").
			header("X-mutua-channel", "APP-MM").
			header("x-ibm-client-id", "20b04c60-d8a2-439d-bc3f-d4f4f1e0fa67").
			relaxedHTTPSValidation().
			log().all().
		when().
			get(baseUrl + "/api/seguros/v1/polizas").
		then().
			//log().all().
		extract().
			response();
		
		globalPositionJsonPath = response.jsonPath();
		
		String nif = globalPositionJsonPath.getString("[0].tomador.nif");
		String holderName = globalPositionJsonPath.getString("[0].tomador.nombre") +
			" " + globalPositionJsonPath.getString("[0].tomador.apellido");
		
		String birthDate = globalPositionJsonPath.getString("[0].tomador.fechaNacimiento");
		String phoneNumber = globalPositionJsonPath.getString("[0].tomador.informacionContacto.telefonoMovil");
		String email = globalPositionJsonPath.getString("[0].tomador.informacionContacto.email");
		String province = globalPositionJsonPath.getString("[0].tomador.informacionContacto.direccion.provincia");
		String city = globalPositionJsonPath.getString("[0].tomador.informacionContacto.direccion.localidad");
		String street = globalPositionJsonPath.getString("[0].tomador.informacionContacto.direccion.nombreVia");
		String postalCode = globalPositionJsonPath.getString("[0].tomador.informacionContacto.direccion.cp");
		String number = globalPositionJsonPath.getString("[0].tomador.informacionContacto.direccion.numero");
		
		var home = new Home();
		home.setProvince(province);
		home.setStreet(street);
		home.setPostalCode(postalCode);
		home.setCity(city);
		home.setNumber(number);
		
		personalInformation.setBirthDate(LocalDate.parse(birthDate, this.getDefaultDateFormatter()));
		personalInformation.setHolderName(holderName);
		personalInformation.setNif(nif);
		personalInformation.setPhoneNumber(phoneNumber);
		personalInformation.setEmailAddress(email);
		personalInformation.setHome(home);
		
		return personalInformation;
	}

	@Override
	public List<HomeInsurance> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsurance>();
		
		Response response = given().
			header("x-mutua-feventsource", "acceder").
			header("User-Agent", ":	okhttp/4.8.1").
			header("Authorization", "Bearer " + authToken).
			header("User-Agent", ":	okhttp/4.8.1").
			header("X-mutua-channel", "APP-MM").
			header("x-ibm-client-id", "20b04c60-d8a2-439d-bc3f-d4f4f1e0fa67").
			relaxedHTTPSValidation().
			log().all().
		when().
			get(baseUrl + "/api/seguros/v1/posicionGlobalDetallada").
		then().
			log().all().
		extract().
			response();
		
		
		List<Map<String, Object>> insurancesList = response.jsonPath().getList("polizasHogar");
		if (insurancesList != null) {
			insurancesList.stream().forEach(i -> {
				Map<String, Object> inmuebleDetail = (Map<String, Object>)i.get("datosPrincipalesHogar");
				Map<String, Object> inmuebleFeatures = (Map<String, Object>)i.get("inmueble");
				String policyNumber = i.get("polizaId").toString();
				String productName = i.get("alias").toString();
				String dueDate = i.get("fechaVencimiento").toString();
				String startingDate = i.get("fechaEstado").toString();
				String iban = ((Map)i.get("cuentaBancaria")).get("numeroCuenta").toString();
				String premium = ((Map)i.get("ultimoRecibo")).get("importe").toString();
				String recurrence = i.get("frecuenciaPago").toString();
				String houseType = ((Map)inmuebleFeatures.get("caracteristicas")).get("tipoVivienda").toString();
				String constructionYear = ((Map)inmuebleFeatures.get("caracteristicas")).get("anoConstruccion").toString();
				String squaredMetres = ((Map)((Map)inmuebleFeatures.get("caracteristicas")).get("dimensiones")).get("superficieConstruida").toString();
				
				
				var insurance = new HomeInsurance();
				Home asseguredHome = new Home();
				
				insurance.setProductName(productName);
				insurance.setProductId(policyNumber);
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter()));
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter()));
				insurance.setIban(iban);
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setRecurrence(recurrence);
				insurance.setHouseType(houseType);
				insurance.setConstructionYear(constructionYear);
				insurance.setSquaredMetres(squaredMetres);
				
				asseguredHome.setCity(((Map)inmuebleDetail.get("inmueble")).get("localidad").toString());
				asseguredHome.setNumber(((Map)inmuebleDetail.get("inmueble")).get("numero").toString());
				asseguredHome.setPostalCode(((Map)inmuebleDetail.get("inmueble")).get("cp").toString());
				asseguredHome.setProvince(((Map)inmuebleDetail.get("inmueble")).get("provincia").toString());
				asseguredHome.setStreet(((Map)inmuebleDetail.get("inmueble")).get("nombreVia").toString());
				asseguredHome.setStreetType(((Map)inmuebleDetail.get("inmueble")).get("tipoVia").toString());
				
				List<Map<String, Object>> coverages = (List<Map<String, Object>>)i.get("garantias");
				List<Coverage> coverageList = coverages.stream().map(c -> new Coverage(c.get("descripcion").toString(), 
						c.get("valorCubierto") != null ? Double.parseDouble(c.get("valorCubierto").toString()): null)).
						collect(Collectors.toList());
				insurance.setCoverages(coverageList);
			
				insurance.setAsseguredHome(asseguredHome);
				
				
				insurances.add(insurance);
			});
		}
		
		return insurances;
	}

	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		List<Map<String, Object>> insurancesList = globalPositionJsonPath.getList("$");
		insurancesList.stream().
			filter(c -> c.get("descripcionRamo").toString().contains("AUTOS")).
			forEach(i -> {
				String policyNumber = i.get("polizaId").toString();
				String productName = i.get("descripcionProducto").toString();
				String dueDate = i.get("fechaVencimiento").toString();
				String startingDate = i.get("fechaEstado").toString();
				String premium = ((Map)i.get("ultimoRecibo")).get("importe").toString();
				String paymentType = i.get("frecuenciaPago").toString();
				String iban = ((Map)i.get("cuentaBancaria")).get("numeroCuenta").toString();
				
				String carPlate = ((Map)i.get("vehiculo")).get("matricula").toString();
				String model = ((Map)i.get("vehiculo")).get("modelo").toString();
				String brand = ((Map)i.get("vehiculo")).get("marca").toString();
				String vehicleType = ((Map)i.get("vehiculo")).get("tipoVehiculo").toString();
				String extendedModelInfo = "Submodelo: " + ((Map)i.get("vehiculo")).get("submodelo").toString() + ", Potencia:" +
						((Map)i.get("vehiculo")).get("potencia") + ", Puerta:" + ((Map)i.get("vehiculo")).get("puerta") + ", Ocupantes:" + 
						((Map)i.get("vehiculo")).get("ocupantes");
				Integer registrationYear = ((Map)i.get("vehiculo")).get("primeraMatriculacion").toString() != null ? 
						Integer.parseInt(((Map)i.get("vehiculo")).get("primeraMatriculacion").toString().substring(0, 4)): null;
						
						
				String mainDriverName = ((Map)i.get("conductorHabitual")).get("nombre").toString() + " " +
							((Map)i.get("conductorHabitual")).get("apellido").toString();
				String mainDriverNif = ((Map)i.get("conductorHabitual")).get("nif").toString();
				String mainDriverBirthDate = ((Map)i.get("conductorHabitual")).get("fechaNacimiento").toString();
				String mainDriverAntigCarnet = ((Map)i.get("conductorHabitual")).get("fechaExpedicionCarnet").toString();
				Integer antigCarnet = LocalDate.now().getYear() - LocalDate.parse(mainDriverAntigCarnet, this.getDefaultDateFormatter()).getYear();
				
				List<Map<String, Object>> coverages = (List<Map<String, Object>>)i.get("garantias");
				List<Coverage> coverageList = coverages.stream().map(c -> new Coverage(c.get("descripcion").toString(), 
						c.get("valorCubierto") != null ? Double.parseDouble(c.get("valorCubierto").toString()): null)).
						collect(Collectors.toList());
				
				var insurance = new CarInsurance();
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter()));
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter()));
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setProductId(policyNumber);
				insurance.setProductName(productName);
				insurance.setRecurrence(paymentType);
				insurance.setIban(iban);
				insurance.setCarPlate(carPlate);
				insurance.setModel(model);
				insurance.setBrand(brand);
				insurance.setExtendedModelInfo(extendedModelInfo);
				insurance.setRegistrationYear(registrationYear);
				insurance.setMainDriverBirthDate(LocalDate.parse(mainDriverBirthDate, this.getDefaultDateFormatter()));
				insurance.setMainDriverName(mainDriverName);
				insurance.setMainDriverNif(mainDriverNif);
				insurance.setMainDriverAgeOfCarnet(antigCarnet);
				insurance.setVehicleType(vehicleType);
				insurance.setCoverages(coverageList);
				insurances.add(insurance);
			});
		
		return insurances;
	}

}
