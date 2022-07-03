package com.fpnatools.aggregation.insurances.framework.adapters;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fpnatools.aggregation.insurances.framework.restAPI.dto.CarInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.HomeInsuranceDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.PersonalInformationDTO;

public interface RobotAdapter {

	public boolean login(Map<String, String> credentials);
	
	public PersonalInformationDTO getPersonalInformation();
	
	public List<HomeInsuranceDTO> getHomeInsurances();
	
	public List<CarInsuranceDTO> getCarInsurances();
	
	
	public default DateTimeFormatter getDefaultDateFormatter() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		return dateFormatter;
	}
	
	public default DateTimeFormatter getDefaultDateFormatter2() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		return dateFormatter;
	}
	
}
