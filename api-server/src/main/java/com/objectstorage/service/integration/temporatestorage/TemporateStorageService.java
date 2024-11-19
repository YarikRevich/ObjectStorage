package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.config.ConfigService;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides http server configuration used as a source of properties for all defined resources.
 */
@Startup(value = 800)
@ApplicationScoped
public class TemporateStorageService {
    @Inject
    ConfigService configService;

    @Inject
    RepositoryFacade repositoryFacade;

    /**
     * Performs temporate storage configuration.
     */
    @PostConstruct
    public void process() {

    }
}

