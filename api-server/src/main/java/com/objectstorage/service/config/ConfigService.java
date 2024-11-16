package com.objectstorage.service.config;

import com.objectstorage.entity.common.ConfigEntity;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.ConfigFileClosureFailureException;
import com.objectstorage.exception.ConfigFileNotFoundException;
import com.objectstorage.exception.ConfigFileReadingFailureException;
import com.objectstorage.exception.ConfigValidationException;
import com.objectstorage.service.config.common.ConfigConfigurationHelper;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.Getter;

/**
 * Service used to perform ObjectStorage API Server configuration processing
 * operation.
 */
@Startup
@ApplicationScoped
public class ConfigService {
    @Inject
    PropertiesEntity properties;

    @Getter
    private ConfigEntity config;

    /**
     * Reads configuration from the opened configuration file using mapping with a
     * configuration entity.
     *
     * @throws ConfigFileNotFoundException       if configuration file is not found.
     * @throws ConfigValidationException         if configuration file operation
     *                                           failed.
     * @throws ConfigFileReadingFailureException if configuration file reading
     *                                           operation failed.
     * @throws ConfigFileClosureFailureException if configuration file closure
     *                                           operation failed.
     */
    @PostConstruct
    private void configure() throws ConfigFileNotFoundException,
            ConfigValidationException,
            ConfigFileReadingFailureException,
            ConfigFileClosureFailureException {
        config = ConfigConfigurationHelper.readConfig(properties.getConfigLocation(), true);
    }
}
