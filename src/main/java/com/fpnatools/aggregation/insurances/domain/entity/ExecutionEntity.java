package com.fpnatools.aggregation.insurances.domain.entity;

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

import com.fpnatools.aggregation.insurances.domain.entity.vo.ExecutionStatus;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_EXECUTIONS")
public class ExecutionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@ManyToOne
	@JoinColumn(name = "INSURANCE_COMPANY_ID")
	private InsuranceCompanyEntity insuranceCompanyEntity;
	
	@CreationTimestamp
	private LocalDateTime timestamp;
	
	@ManyToOne
	private UserEntity user;
	
	private String username;
	
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus;
	
	private String errorDescription;
	
	private Double totalDuration;
	
	private String traceId;
	
}
