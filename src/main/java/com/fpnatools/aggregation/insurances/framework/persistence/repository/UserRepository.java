package com.fpnatools.aggregation.insurances.framework.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;


public interface UserRepository extends JpaRepository<AppUser, Long> {

	public Optional<AppUser> findByAppUser(String appUser);
}
