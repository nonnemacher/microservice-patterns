package com.satish.central.docs.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.satish.central.docs.config.swagger.ServiceDefinitionsContext;

import java.util.Optional;

/**
 * 
 * @author satish sharma
 * <pre>
 *  Controller to serve the JSON from our in-memory store. So that UI can render the API-Documentation	
 * </pre>
 */
@RestController
public class ServiceDefinitionController {
	
	@Autowired
	private ServiceDefinitionsContext definitionContext;

	@GetMapping("/service/{serviceName}")
	public ResponseEntity<?> getServiceDefinition(@PathVariable String serviceName){
		return Optional.ofNullable(definitionContext.getSwaggerDefinition(serviceName))
				.map(ResponseEntity::ok)
				.orElseGet(ResponseEntity.notFound()::build);
	}
}
