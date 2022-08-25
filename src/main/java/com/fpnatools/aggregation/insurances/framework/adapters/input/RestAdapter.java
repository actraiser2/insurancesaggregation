package com.fpnatools.aggregation.insurances.framework.adapters.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.validation.Valid;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.domain.commands.CreateExecutionCommand;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider.Proxy;

@RestController
@RequestMapping("/api/executions")
@AllArgsConstructor
@SecurityRequirement(name = "Basic")
@Slf4j
public class RestAdapter {

	private CreateExecutionUseCase createExecutionUseCase;
	private GetExecutionStatusUseCase getExecutionStatusUseCase;
	private Environment env;
	
	@PostMapping
	public Mono<ResponseEntity<ExecutionResponseDTO>> createExecution(@Valid @RequestBody CreateExecutionCommand command, Authentication auth) {
		log.info("Creating execution ");
		Long executionId = createExecutionUseCase.createExecution(auth.getName(), command);
		return Mono.just(ResponseEntity.status(201).body(new ExecutionResponseDTO(executionId)));
	}
	
	@GetMapping(value = "/{executionId}")
	@Operation(summary = "Get the status of the execution")
	public Mono<AggregationResult> getExecutionStatus(
			@Parameter(description = "Identification of the execution") @PathVariable("executionId") Long executionId) {
		log.info("Querying execution ");
		return Mono.fromCallable(() -> getExecutionStatusUseCase.getExecutionStatus(executionId)).publishOn(Schedulers.boundedElastic());
	}
	
	@GetMapping(value = "/echo", headers={"X-Api-Version=v1"})
	public Flux<String>echo(JwtAuthenticationToken auth) throws InterruptedException, IOException {
		String basePathCertificates = env.getProperty("certificates.path", "/certificates");
		log.info("before -> cALLING METHOD ECHO:" + 
				Files.size(Path.of(basePathCertificates, "finreach.jks")) + " bytes");
		
		HttpClient httpClient = HttpClient.create().
	            tcpConfiguration(tcpClient -> tcpClient
	                .proxy(proxy -> proxy.type(Proxy.HTTP)
	                    .host("localhost")
	                    .port(8080)));

	ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);


		WebClient client = WebClient.builder().baseUrl("https://www.google.es").
				defaultHeader("Authorization", "Bearer aaaaa").
				//clientConnector(connector).
				build();
		
		
		return client.get().exchangeToFlux(r -> {
			log.info("Status:" + r.statusCode());
			log.info("Headers:" + r.headers().asHttpHeaders());
			if (r.statusCode().value() != 200) {
				return Flux.empty();
			}
			else {
				return Flux.fromIterable(r.headers().asHttpHeaders().keySet());
			}
			
			
		});
		
	}
}
