package com.objectstorage.resource;

import com.objectstorage.api.HealthResourceApi;
import com.objectstorage.model.HealthCheckResult;
import com.objectstorage.service.client.smallrye.ISmallRyeHealthCheckClientService;
import com.objectstorage.service.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

/**
 * Contains implementation of HealthResource.
 */
@ApplicationScoped
public class HealthResource implements HealthResourceApi {
    @Inject
    ConfigService configService;

    ISmallRyeHealthCheckClientService smallRyeHealthCheckClientService;

    @PostConstruct
    private void configure() {
        smallRyeHealthCheckClientService = RestClientBuilder.newBuilder()
                .baseUri(
                        URI.create(
                                String.format(
                                        "http://localhost:%d", configService.getConfig().getConnection().getPort())))
                .build(ISmallRyeHealthCheckClientService.class);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1HealthGet method.
     *
     * @return health check result.
     */
    @Override
    public HealthCheckResult v1HealthGet() {
        try {
            return smallRyeHealthCheckClientService.qHealthGet();
        } catch (WebApplicationException e) {
            return e.getResponse().readEntity(HealthCheckResult.class);
        }
    }
}
