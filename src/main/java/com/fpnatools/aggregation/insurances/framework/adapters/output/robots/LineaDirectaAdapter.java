package com.fpnatools.aggregation.insurances.framework.adapters.output.robots;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
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
import lombok.extern.log4j.Log4j2;

@Component("LineaDirectaAdapter")
@Scope("prototype")
@Log4j2
public class LineaDirectaAdapter implements RobotOutputPort {

	private String baseUrl;
	protected String app = "ENRUTA";
	private String sessionToken;
	private JsonPath globalPositionJsonPath;
	
	@Override
	public boolean login(Map<String, String> credentials) {
		// TODO Auto-generated method stub
		baseUrl = "https://www.lineadirecta.com/enruta/rest";
		
		String username = credentials.get("username");
		String password = credentials.get("password");
		
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
			return false;
		}

		Response response = given().
			contentType("application/x-www-form-urlencoded").
			formParam("documento", username.trim()).
			formParam("clave", password).
			formParam("app", app).
			log().all().
		when().
			post(baseUrl + "/usuario/loginapp/v1").
		then().
			log().all().
		extract().
			response();
	
		if (response.statusCode() == 200) {
			String responseAsText = response.asString();
			if (!responseAsText.contains("incorrectos") && !responseAsText.contains("Documento")) {
				sessionToken = responseAsText;
				return true;
			}
			
		}
		else if (response.statusCode() != 401 && !response.asString().contains("Cuenta bloqueada")) {
			throw new GenericAggregationException(response.asString());
		}
	
		return false;
	}

	@Override
	public PersonalInformation getPersonalInformation() {
		// TODO Auto-generated method stub
		
		Response response = given().
			header("authorization", sessionToken).
			log().all().
		when().
			get(baseUrl + "/cliente/buscarClientePorDocumento/v1?idioma=ES").
		then().
			//log().all().
		extract().
			response();
		
		globalPositionJsonPath = response.jsonPath();
		
		var personalInformation = new PersonalInformation();
		
		String nif = globalPositionJsonPath.getString("documento");
		String holderName = globalPositionJsonPath.getString("nombre") + " " +
				globalPositionJsonPath.getString("apellido1") + " " + 
				globalPositionJsonPath.getString("apellido2");
		
		String birthDate = globalPositionJsonPath.getString("fechaNacimiento");
		String phoneNumber = globalPositionJsonPath.getString("beanNotificacion.movilNormal");
		String email = globalPositionJsonPath.getString("beanNotificacion.emailNormal");
		String province = globalPositionJsonPath.getString("beanNotificacion.descri30Provincia");
		String city = globalPositionJsonPath.getString("beanNotificacion.descri50Poblacion");
		String street = globalPositionJsonPath.getString("beanNotificacion.descri50Via");
		String postalCode = globalPositionJsonPath.getString("beanNotificacion.codPostal");
		
		var home = new Home();
		home.setProvince(province);
		home.setStreet(street);
		home.setPostalCode(postalCode);
		home.setCity(city);
		
		personalInformation.setBirthDate(LocalDate.parse(birthDate, this.getDefaultDateFormatter2()));
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
		
		List<Map<String, Object>> homeInsuranceList = globalPositionJsonPath.getList("polizasHogar");
		
		homeInsuranceList.stream().
			filter(p -> p.get("descEstado").equals("Activa")).
			forEach(p -> {
				String policyNumber = p.get("polRef").toString();
				String productName = p.get("descProducto").toString();
				String dueDate = p.get("fechaVencimiento").toString();
				String startingDate = p.get("fechaVigor").toString();
				String premium = p.get("prima").toString();
				String paymentType = p.get("descTipoPago").toString();	
				
				Map<String, Object> beanDomicilio = (Map<String, Object>)p.get("beanDomicilioInternet");
				
				String rawInsurancedHomeAddress = beanDomicilio.get("descri50Via") + " Numero:" + beanDomicilio.get("numero") +
						" Piso:" + beanDomicilio.get("piso") + " " + beanDomicilio.get("codLocalidad")  + "(" + beanDomicilio.get("codPostal") + ")";
				
				
				Map<String, String> polizaParameters = new HashMap<>();
				polizaParameters.put("fechaVigorSesion", p.get("fechaVigor").toString());
				polizaParameters.put("idioma", "ES");
				polizaParameters.put("numSecRenov", p.get("numsecRenov").toString());
				polizaParameters.put("numSecVar", p.get("numsecVar").toString());
				polizaParameters.put("polref", p.get("polRef").toString());
				
				Response policyDetailResponse = given().
					header("authorization", sessionToken).
					header("User-Agent", "	okhttp/4.1.0").
					contentType("application/json").
					body(polizaParameters).
					log().all().
				when().
					post(baseUrl + "/cliente/polizasHogar/getPolizahogar/v2").
				then().
					//log().all().
				extract().
					response();
				
				JsonPath policyDetailJsonPath = policyDetailResponse.jsonPath();
				
				String iban = policyDetailJsonPath.getString("objDatosNumCuenta.iban");
				String constructionYear =  policyDetailJsonPath.getString("datosViviendaHogar.antiguedad");
				String houseType = policyDetailJsonPath.getString("datosViviendaHogar.descTipo");
				String capitalMuebles = policyDetailJsonPath.getString("capitalMueblesInteger");
				String capitalViviendaInteger = policyDetailJsonPath.getString("capitalViviendaInteger");
				String homeAdditionalDetail = policyDetailJsonPath.get("datosViviendaHogar").toString();
				
				var coverages = new ArrayList<Coverage>();
				coverages.add(new Coverage("Capital Muebles", Double.parseDouble(capitalMuebles)));
				coverages.add(new Coverage("Capital Vivienda", Double.parseDouble(capitalViviendaInteger)));
		
				var insurance = new HomeInsurance();
				Home asseguredHome = new Home();
				asseguredHome.setCity(beanDomicilio.get("codLocalidad").toString());
				asseguredHome.setProvince(beanDomicilio.get("descri30Provincia").toString());
				asseguredHome.setPostalCode(beanDomicilio.get("codPostal").toString());
				asseguredHome.setStreet(beanDomicilio.get("descri50Via").toString());
				asseguredHome.setStreetType(beanDomicilio.get("tipoVia").toString());
				asseguredHome.setFloor(beanDomicilio.get("piso").toString());
				asseguredHome.setDoor(beanDomicilio.get("puerta").toString());
				asseguredHome.setNumber(beanDomicilio.get("numero").toString());
				asseguredHome.setRawAddress(rawInsurancedHomeAddress);
				
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter2()));
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter2()));
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setProductId(policyNumber);
				insurance.setProductName(productName);
				insurance.setRecurrence(paymentType);
				insurance.setAsseguredHome(asseguredHome);
				insurance.setConstructionYear(constructionYear);
				insurance.setHouseType(houseType);
				insurance.setCoverages(coverages);
				insurance.setHomeAdditionalDetail(homeAdditionalDetail);
				insurance.setIban(iban);
				insurances.add(insurance);
			});
			
		return insurances;
	}
	
	@Override
	public List<CarInsurance> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsurance>();
		
		List<Map<String, Object>> carInsuranceList = globalPositionJsonPath.getList("polizasCoches");
		carInsuranceList.stream().
		filter(p -> p.get("descEstado").equals("Activa")).
		forEach(p -> {
			String policyNumber = p.get("polRef").toString();
			String productName = p.get("descProducto").toString();
			String dueDate = p.get("fechaVencimiento").toString();
			String startingDate = p.get("fechaVigor").toString();
			String premium = p.get("prima").toString();
			String model = p.get("refModelo").toString();
			String brand = p.get("refMarca").toString();
			String carPlate = p.get("refMat").toString();
			String vehicleExtendedInf = p.get("refVersion").toString();
			
			Map<String, Object> beanDomicilio = (Map<String, Object>)p.get("beanDomicilioInternet");
	
			String city = beanDomicilio.get("codLocalidad").toString();
			String province = beanDomicilio.get("descri30Provincia").toString();
			String street = beanDomicilio.get("descri50Via").toString();
			String postalCode = beanDomicilio.get("codPostal").toString();
			
			var home = new Home();
			home.setCity(city);
			home.setProvince(province);
			home.setPostalCode(postalCode);
			home.setStreet(street);
			
			var insurance = new CarInsurance();
			
			insurance.setProductId(policyNumber);
			insurance.setProductName(productName);
			insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter2()));
			insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter2()));
			insurance.setPremium(Double.parseDouble(premium));
			insurance.setBrand(brand);
			insurance.setCarPlate(carPlate);
			insurance.setModel(model);
			//insurance.setHome(home);
			insurance.setExtendedModelInfo(vehicleExtendedInf);
			insurances.add(insurance);
			
			
			Map<String, String> polizaParameters = new HashMap<>();
			polizaParameters.put("fechaVigorSesion", p.get("fechaVigor").toString());
			polizaParameters.put("idioma", "ES");
			polizaParameters.put("indVehiculo", p.get("indVehiculo").toString());
			polizaParameters.put("numsecRenov", p.get("numsecRenov").toString());
			polizaParameters.put("numsecVar", p.get("numsecVar").toString());
			polizaParameters.put("polRef", p.get("polRef").toString());
			polizaParameters.put("stContrato", "");
			
			Response policyDetailResponse = given().
				header("authorization", sessionToken).
				header("User-Agent", "	okhttp/4.1.0").
				contentType("application/json").
				body(polizaParameters).
				log().all().
			when().
				post(baseUrl + "/cliente/polizas/getGestionPolizaMotor/v1").
			then().
				//log().all().
			extract().
				response();
			
			JsonPath policyDetail = policyDetailResponse.jsonPath();
			
			String mainDriverName = policyDetail.getString("objDatosFiguras.objDatosConductorPrincipal.nombre") + " " +
					policyDetail.getString("objDatosFiguras.objDatosConductorPrincipal.apellido1") + " " +
					policyDetail.getString("objDatosFiguras.objDatosConductorPrincipal.apellido2");
			
			String mainDriverNif = policyDetail.getString("objDatosFiguras.objDatosConductorPrincipal.documento");
			//String mainDriverBirthDate = policyDetail.getString("fechaNacimiento");
			String antigCarnet = policyDetail.getString("objDatosFiguras.objDatosConductorPrincipal.antigCarnet");
			String iban = policyDetail.getString("objDatosNumCuenta.iban");
			
			
			List<String> coverageList = policyDetail.getList("coberturas");
			
			List<Coverage> coverages = coverageList.stream().map(c -> {
				var coverage = new Coverage();
				coverage.setName(c);
				return coverage;
			}).collect(Collectors.toList());
			
			insurance.setMainDriverNif(mainDriverNif);
			insurance.setMainDriverName(mainDriverName);
			insurance.setMainDriverAgeOfCarnet(Integer.parseInt(antigCarnet));
			insurance.setIban(iban);
			insurance.setCoverages(coverages);
			insurance.setMainDriverName(mainDriverName);
			
			
		});
		
		return insurances;
	}

}
