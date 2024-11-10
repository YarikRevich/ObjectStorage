package com.repoachiever.service.integration.backup;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides http server configuration used as a source of properties for all defined resources.
 */
@ApplicationScoped
public class BackupService {
    @Inject
    com.objectstorage.service.config.ConfigService configService;

    /**
     * Performs temporate storage configuration.
     */
    @PostConstruct
    public void process() {

    }
}