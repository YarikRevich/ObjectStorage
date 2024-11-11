package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.service.config.ConfigService;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides http server configuration used as a source of properties for all defined resources.
 */
@ApplicationScoped
public class TemporateStorageService {
    @Inject
    ConfigService configService;

    /**
     * Performs temporate storage configuration.
     */
    @PostConstruct
    public void process() {

    }
}

