package com.fpnatools.aggregation.insurances.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(
        name = "Basic",
        type = SecuritySchemeType.HTTP,
        description = "Basic authentication, eg Basic 12345677"
        
)
public class OpenApiConfiguration {

	 @Bean
	  public OpenAPI publicApi() {
	      return new OpenAPI().
	    		  info(new Info().title("Insurance Aggregation").
	    				  summary("Apllication for aggregating insurances"));
	             
	  }
}
