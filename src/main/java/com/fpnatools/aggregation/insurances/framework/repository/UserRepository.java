package com.fpnatools.aggregation.insurances.framework.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Long> {

	public Optional<UserEntity> findByAppUser(String appUser);
}
