package com.fpnatools.aggregation.insurances.framework.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		var passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder;
		
	}
	
	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity httpSecurity) throws Exception {
		return httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt()).

			authorizeExchange().
				pathMatchers("/executions/**").hasAuthority("SCOPE_insurances").
				pathMatchers("/**").permitAll().
			and().
			//addFilterBefore(requestValidationFilter, BasicAuthenticationFilter.class).
		
			csrf().disable().build();
	}
	
	//@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return appUser -> {
			Optional<AppUser> user = userRepository.findByAppUser(appUser);
			log.info("AppUser:" + user.get().getAppUser());
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
	
	  @Bean
	  public CorsWebFilter  corsFilter() {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    //config.setAllowCredentials(true);
	    config.addAllowedOrigin("*");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("*");
	    source.registerCorsConfiguration("/graphql/**", config);
	    return new CorsWebFilter (source);
	  }
	
	
}
