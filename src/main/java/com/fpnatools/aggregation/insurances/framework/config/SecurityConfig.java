package com.fpnatools.aggregation.insurances.framework.config;

import java.util.Optional;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fpnatools.aggregation.insurances.domain.entity.AppUser;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		var passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder;
		
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity, Filter requestValidationFilter) throws Exception {
		return httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt()).

			authorizeHttpRequests().
				antMatchers("/executions/**").hasAuthority("SCOPE_insurances").
				antMatchers("/**").permitAll().
			and().
			//addFilterBefore(requestValidationFilter, BasicAuthenticationFilter.class).
			csrf().disable().build();
	}
	
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return appUser -> {
			Optional<AppUser> user = userRepository.findByAppUser(appUser);
			
			if (user.isPresent()) {
				UserDetails userDetails = User.withUsername(appUser)
						.roles(user.get().getRoleName()).password(user.get().getPassword()).build();
				return userDetails;
				
			}
			else {
				throw new UsernameNotFoundException("User " + appUser + " does not exist");
			}
		};
	}
	
	
}
