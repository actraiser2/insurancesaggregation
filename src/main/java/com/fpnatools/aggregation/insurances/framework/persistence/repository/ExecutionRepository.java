package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.entity.Execution;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

}
