package com.fpnatools.aggregation.insurances.framework.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompanyEntity;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompanyEntity, Long> {

	public Optional<InsuranceCompanyEntity> findByName(String name);
}
