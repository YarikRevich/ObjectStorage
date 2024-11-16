package com.objectstorage.service.config.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.logging.log4j.core.util.CronExpression;

import com.objectstorage.exception.ConfigFileClosureFailureException;
import com.objectstorage.exception.ConfigFileNotFoundException;
import com.objectstorage.exception.ConfigFileReadingFailureException;
import com.objectstorage.exception.ConfigValidationException;
import com.objectstorage.entity.common.ConfigEntity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains helpful tools used for config configuration.
 */
public class ConfigConfigurationHelper {
    /**
     * Reads configuration from the opened configuration file using mapping with a
     * configuration entity.
     *
     * @param configLocation given configuration file location.
     * @param validation enables persistence validation. Meant to be disabled for initialization phase.
     * @return read configuration file at the given location.
     * @throws ConfigFileNotFoundException       if configuration file is not found.
     * @throws ConfigValidationException         if configuration file operation
     *                                           failed.
     * @throws ConfigFileReadingFailureException if configuration file reading
     *                                           operation failed.
     * @throws ConfigFileClosureFailureException if configuration file closure
     *                                           operation failed.
     */
    public static ConfigEntity readConfig(String configLocation, Boolean validation) throws ConfigFileNotFoundException,
            ConfigValidationException,
            ConfigFileReadingFailureException,
            ConfigFileClosureFailureException {
        ConfigEntity config;

        InputStream file = null;

        try {
            try {
                file = new FileInputStream(Paths.get(configLocation).toString());
            } catch (FileNotFoundException e) {
                throw new com.objectstorage.exception.ConfigFileNotFoundException(e.getMessage());
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                    .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            ObjectReader reader = mapper.reader().forType(new TypeReference<com.objectstorage.entity.common.ConfigEntity>() {
            });

            try {
                List<com.objectstorage.entity.common.ConfigEntity> values = reader.<com.objectstorage.entity.common.ConfigEntity>readValues(file).readAll();
                if (values.isEmpty()) {
                    return null;
                }

                config = values.getFirst();
            } catch (IOException e) {
                throw new com.objectstorage.exception.ConfigFileReadingFailureException(e.getMessage());
            }

            if (validation) {
                try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
                    Validator validator = validatorFactory.getValidator();

                    Set<ConstraintViolation<ConfigEntity>> validationResult = validator.validate(config);

                    if (!validationResult.isEmpty()) {
                        throw new com.objectstorage.exception.ConfigValidationException(
                                validationResult.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining(", ")));
                    }
                }
            }

            if (!CronExpression.isValidExpression(
                    config.getTemporateStorage().getFrequency())) {
                throw new com.objectstorage.exception.ConfigValidationException(
                        new com.objectstorage.exception.ConfigCronExpressionValidationException().getMessage());
            }
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                throw new com.objectstorage.exception.ConfigFileClosureFailureException(e.getMessage());
            }
        }

        return config;
    }
}
