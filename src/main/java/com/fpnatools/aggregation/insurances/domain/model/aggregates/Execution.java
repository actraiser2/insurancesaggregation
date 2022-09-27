package com.fpnatools.aggregation.insurances.domain.model.aggregates;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.AbstractAggregateRoot;

import com.fpnatools.aggregation.insurances.domain.commands.CreateExecutionCommand;
import com.fpnatools.aggregation.insurances.domain.commands.UpdateExecutionCommand;
import com.fpnatools.aggregation.insurances.domain.events.CreatedExecutionEvent;
import com.fpnatools.aggregation.insurances.domain.events.UpdatedExecutionEvent;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.ExecutionStatus;
import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;
import com.fpnatools.aggregation.insurances.domain.model.entities.InsuranceCompany;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Table(name = "TB_EXECUTIONS")
@Slf4j
@NoArgsConstructor
public class Execution extends AbstractAggregateRoot<Execution>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@ManyToOne
	@JoinColumn(name = "INSURANCE_COMPANY_ID")
	private InsuranceCompany insuranceCompanyEntity;
	
	@CreationTimestamp
	private LocalDateTime timestamp;
	
	@ManyToOne
	private AppUser user;
	
	private String username;
	
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus;
	
	private String errorDescription;
	
	private Double totalDuration;
	
	private String traceId;
	
	
	public Execution(CreateExecutionCommand command) {
		log.info("Executing command CreateExecutionCommand " + command);
		
		this.setExecutionStatus(ExecutionStatus.ONGOING);
		this.setUser(command.getAppUser());
		this.setUsername(command.getCredentials().get("username"));
		this.setInsuranceCompanyEntity(command.getInsuranceCompany());
		this.registerEvent(new CreatedExecutionEvent(this, command.getCredentials()));
	
	}
	
	public void updateExecution(UpdateExecutionCommand command) {
		log.info("Executing command UpdateExecutionCommand " + command);
		
		this.setErrorDescription(command.getErrorDescription());
		this.setExecutionStatus(command.getExecutionStatus());
		this.setTotalDuration(command.getTotalDuration());
		this.registerEvent(new UpdatedExecutionEvent(this));
	}
	
	
	
}
