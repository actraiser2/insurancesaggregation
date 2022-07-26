package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompany;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Long> {

	public Optional<InsuranceCompany> findByName(String name);
}
