package com.satish.central.docs.config.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;

/**
 * @author satish sharma
 * <pre>
 *   Periodically poll the service instaces and update the in memory store as key value pair
 * </pre>
 */
@Slf4j
@Component
public class ServiceDescriptionUpdater {

    private static final String SWAGGER_RESOURCES = "/swagger-resources";

    @Autowired
    private DiscoveryClient discoveryClient;

    private final RestTemplate template;

    public ServiceDescriptionUpdater() {
        this.template = new RestTemplate();
    }

    @Autowired
    private ServiceDefinitionsContext definitionContext;

    @Scheduled(fixedDelayString = "${swagger.config.refreshrate}")
    public void refreshSwaggerConfigurations() {
        log.debug("Starting Service Definition Context refresh");

        discoveryClient.getServices().stream().forEach(serviceId -> {

            log.debug("Attempting service definition refresh for Service : {} ", serviceId);
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);

            if (serviceInstances == null || serviceInstances.isEmpty()) { //Should not be the case kept for failsafe
                log.info("No instances available for service : {} ", serviceId);
            } else {
                serviceInstances.stream()
                        .findFirst()
                        .ifPresent(instance -> {
                            getSwaggerURL(instance).parallelStream()
                                    .forEach(swaggerResponse -> {
                                        Optional<Object> jsonData = getSwaggerDefinitionForAPI(serviceId, instance, swaggerResponse.getLocation());
                                        if (jsonData.isPresent()) {
                                            String content = getJSON(serviceId, jsonData.get());
                                            definitionContext.addServiceDefinition(serviceId + " - " + swaggerResponse.getName(), content);
                                        } else {
                                            log.error("Skipping service id : {} Error : Could not get Swagegr definition from API ", serviceId);
                                        }
                                    });
                            log.info("Service Definition Context Refreshed at :  {}", LocalDate.now());
                        });
            }
        });
    }

    private Set<SwaggerResponse> getSwaggerURL(ServiceInstance instance) {
        final String url = instance.getUri() + SWAGGER_RESOURCES;
        try {

            final ResponseEntity<List<SwaggerResponse>> response = template.exchange(url, GET, null, new ParameterizedTypeReference<List<SwaggerResponse>>() {
            });

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return response.getBody().stream().collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("Error while getting swagger definition for URL : {} Error : {} ", url, e.getMessage());
        }
        return Collections.EMPTY_SET;
    }

    private Optional<Object> getSwaggerDefinitionForAPI(String serviceName, ServiceInstance instance, String url) {
        log.debug("Accessing the SwaggerDefinition JSON for Service : {} : URL : {} ", serviceName, url);
        try {
            Object jsonData = template.getForObject(instance.getUri() + url, Object.class);
            return Optional.of(jsonData);
        } catch (Exception ex) {
            log.error("Error while getting service definition for service : {} Error : {} ", serviceName, ex.getMessage());
            return Optional.empty();
        }

    }

    public String getJSON(String serviceId, Object jsonData) {
        try {
            return new ObjectMapper().writeValueAsString(jsonData);
        } catch (JsonProcessingException e) {
            log.error("Error : {} ", e.getMessage());
            return "";
        }
    }

    @Getter
    @Setter
    public static class SwaggerResponse {
        private String location, swaggerVersion, name, url;
    }

}
