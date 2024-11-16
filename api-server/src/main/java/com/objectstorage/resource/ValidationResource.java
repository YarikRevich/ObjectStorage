package com.objectstorage.resource;

import com.objectstorage.dto.SecretsCacheDto;
import com.objectstorage.dto.ValidationSecretsCompoundDto;
import com.objectstorage.exception.ProvidersAreNotValidException;
import com.objectstorage.model.ValidationSecretsUnit;
import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.api.ValidationResourceApi;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsApplicationResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import java.util.List;

/**
 * Contains implementation of ValidationResource.
 */
@ApplicationScoped
public class ValidationResource implements ValidationResourceApi {
    @Inject
    ResourceConfigurationHelper resourceConfigurationHelper;

    /**
     * Implementation for declared in OpenAPI configuration v1SecretsAcquirePost method.
     *
     * @return created jwt token.
     */
    @Override
    @SneakyThrows
    public ValidationSecretsApplicationResult v1SecretsAcquirePost(ValidationSecretsApplication validationSecretsApplication) {
        if (resourceConfigurationHelper.areProvidersDuplicated(
                validationSecretsApplication
                        .getSecrets()
                        .stream()
                        .map(ValidationSecretsUnit::getProvider)
                        .toList())) {
            throw new ProvidersAreNotValidException();
        };

        List<ValidationSecretsCompoundDto> externalSecrets =
                resourceConfigurationHelper.getExternalSecrets(validationSecretsApplication);

        return ValidationSecretsApplicationResult.of(
                resourceConfigurationHelper.createJwtToken(
                        SecretsCacheDto.of(externalSecrets)));
    }
}
