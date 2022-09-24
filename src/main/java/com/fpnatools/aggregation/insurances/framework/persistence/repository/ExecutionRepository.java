package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.proyections.ExecutionWithCompanyNameDTO;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

	@Query("select e.id, i.name from Execution e, InsuranceCompany i")
	public List<ExecutionWithCompanyNameDTO> findAllExecutionsProjected();
	
	public List<Execution> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
