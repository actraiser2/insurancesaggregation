package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpnatools.aggregation.insurances.domain.entity.Execution;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.proyections.ExecutionWithCompanyNameDTO;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

	@Query("select e.id, i.name from Execution e, InsuranceCompany i")
	public List<ExecutionWithCompanyNameDTO> findAllExecutionsProjected();
}
