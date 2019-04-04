package com.satish.central.docs.config.swagger;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger.web.SwaggerResource;

/**
 * @author satish sharma
 * <pre>
 *   	In-Memory store to hold API-Definition JSON
 * </pre>
 */
@Component
public class ServiceDefinitionsContext {

    private final ConcurrentHashMap<String, String> serviceDescriptions;

    private ServiceDefinitionsContext() {
        serviceDescriptions = new ConcurrentHashMap<>();
    }

    public void addServiceDefinition(String serviceName, String serviceDescription) {
        serviceDescriptions.put(serviceName.toUpperCase(), serviceDescription);
    }

    public String getSwaggerDefinition(String serviceId) {
        return this.serviceDescriptions.get(serviceId);
    }

    public List<SwaggerResource> getSwaggerDefinitions() {
        return serviceDescriptions.entrySet()
                .parallelStream()
                .map(service -> {
                    final SwaggerResource swaggerResource = new SwaggerResource();
                    swaggerResource.setLocation("/service/" + service.getKey());
                    swaggerResource.setName(service.getKey());
                    swaggerResource.setSwaggerVersion("2.0");
                    return swaggerResource;
                })
                .sorted(Comparator.comparing(SwaggerResource::getName))
                .collect(Collectors.toList());
    }
}
