package com.objectstorage.service.secrets.cache;

import com.google.common.cache.Cache;
import com.objectstorage.entity.common.PropertiesEntity;

import com.google.common.cache.CacheBuilder;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.exception.TimeLimitedCacheKeyNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import javax.crypto.KeyGenerator;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents time limited cache implementation used for secrets storage.
 */
@ApplicationScoped
public class TimeLimitedCacheService {
    @Inject
    PropertiesEntity properties;

    private Cache<String, ValidationSecretsApplication> cache;

    @PostConstruct
    private void process() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(properties.getSecretsJwtTtl(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Adds provided secrets validation application to the cache.
     *
     * @param validationSecretsApplication provided secrets validation application to be added to the cache.
     * @return created secrets validation application cached key.
     */
    @SneakyThrows
    public String add(ValidationSecretsApplication validationSecretsApplication) {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

        keyGen.init(128);

        while (true) {
            String key = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());

            if (!exists(key)) {
                cache.put(key, validationSecretsApplication);

                return key;
            }
        }
    }

    /**
     * Checks if secrets validation application is cached with the given key.
     *
     * @param key secrets validation application key expected to be present in the cache.
     * @return result of the check.
     */
    private Boolean exists(String key) {
        try {
            return Objects.nonNull(get(key));
        } catch (TimeLimitedCacheKeyNotFoundException e) {
            return false;
        }
    }

    /**
     * Retrieves secrets validation application from the cache.
     *
     * @param key secrets validation application key to be present in the cache.
     * @return cached secrets validation application.
     */
    public ValidationSecretsApplication get(String key) throws TimeLimitedCacheKeyNotFoundException {
        ValidationSecretsApplication result = cache.getIfPresent(key);

        if (Objects.isNull(result)) {
            throw new TimeLimitedCacheKeyNotFoundException();
        }

        return result;
    }
}
