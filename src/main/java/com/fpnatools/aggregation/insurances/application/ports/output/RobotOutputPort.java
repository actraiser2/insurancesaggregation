package com.fpnatools.aggregation.insurances.application.ports.output;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.CarInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.HomeInsurance;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.PersonalInformation;

public interface RobotOutputPort {

	public boolean login(Map<String, String> credentials);
	
	public PersonalInformation getPersonalInformation();
	
	public List<HomeInsurance> getHomeInsurances();
	
	public List<CarInsurance> getCarInsurances();
	
	public default void releaseResources(){}
	
	
	public default DateTimeFormatter getDefaultDateFormatter() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		return dateFormatter;
	}
	
	public default DateTimeFormatter getDefaultDateFormatter2() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		return dateFormatter;
	}

	
}
