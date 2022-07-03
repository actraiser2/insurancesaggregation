package com.fpnatools.aggregation.insurances.framework.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.entity.ExecutionEntity;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {

}
