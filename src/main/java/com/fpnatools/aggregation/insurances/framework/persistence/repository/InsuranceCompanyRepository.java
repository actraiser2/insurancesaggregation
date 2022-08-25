package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpnatools.aggregation.insurances.domain.model.entities.InsuranceCompany;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Long> {

	public Optional<InsuranceCompany> findByName(String name);
	public Stream<InsuranceCompany> streamAllByTimestampGreaterThanOrderByName(LocalDateTime timesdtamp);
	@Transactional
	@Modifying
	@Query("update InsuranceCompany set name=:name where id=:entityId")
	public void updateName(@Param("entityId") Long entityId,  @Param("name") String name);
}
