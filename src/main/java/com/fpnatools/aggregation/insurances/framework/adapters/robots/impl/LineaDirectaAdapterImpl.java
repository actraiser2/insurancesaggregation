package com.fpnatools.aggregation.insurances.framework.adapters.robots.impl;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

@Component("LineaDirectaAdapter")
@Scope("prototype")
public class LineaDirectaAdapterImpl implements RobotAdapter {

	private static Logger logger = LoggerFactory.getLogger(LineaDirectaAdapterImpl.class);
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
	public PersonalInformationDTO getPersonalInformation() {
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
		
		var personalInformation = new PersonalInformationDTO();
		
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
		
		var home = new HomeDTO();
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
	public List<HomeInsuranceDTO> getHomeInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<HomeInsuranceDTO>();
		
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
				
				var coverages = new ArrayList<CoverageDTO>();
				coverages.add(new CoverageDTO("Capital Muebles", Double.parseDouble(capitalMuebles)));
				coverages.add(new CoverageDTO("Capital Vivienda", Double.parseDouble(capitalViviendaInteger)));
		
				var insurance = new HomeInsuranceDTO();
				insurance.setDueDate(LocalDate.parse(dueDate, this.getDefaultDateFormatter2()));
				insurance.setStartingDate(LocalDate.parse(startingDate, this.getDefaultDateFormatter2()));
				insurance.setPremium(Double.parseDouble(premium));
				insurance.setProductId(policyNumber);
				insurance.setProductName(productName);
				insurance.setRecurrence(paymentType);
				insurance.setRawInsurancedHomeAddress(rawInsurancedHomeAddress);
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
	public List<CarInsuranceDTO> getCarInsurances() {
		// TODO Auto-generated method stub
		var insurances = new ArrayList<CarInsuranceDTO>();
		
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
			
			var home = new HomeDTO();
			home.setCity(city);
			home.setProvince(province);
			home.setPostalCode(postalCode);
			home.setStreet(street);
			
			var insurance = new CarInsuranceDTO();
			
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
			
			List<CoverageDTO> coverages = coverageList.stream().map(c -> {
				var coverage = new CoverageDTO();
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
