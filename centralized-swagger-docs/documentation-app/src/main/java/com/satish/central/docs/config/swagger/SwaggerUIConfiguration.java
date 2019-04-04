package com.satish.central.docs.config.swagger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * 
 * @author satish sharma
 * <pre>
 *  Swagger Ui configurations. Configure bean of the {@link SwaggerResourcesProvider} to
 *   read data from in-memory contex 	
 * </pre>
 */
@Configuration
public class SwaggerUIConfiguration {
	
	@Autowired
	private ServiceDefinitionsContext definitionContext;
	
	@Bean
	public RestTemplate createRestTemplate(){
		return new RestTemplate();
	}

    @Bean
    @Lazy
    @Primary
    public SwaggerResourcesProvider createSwaggerResourcesProvider() {
        return () -> definitionContext.getSwaggerDefinitions();
    }
}

