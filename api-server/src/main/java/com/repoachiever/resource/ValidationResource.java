package com.objectstorage.resource;

import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.api.InfoResourceApi;
import com.objectstorage.api.ValidationResourceApi;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsApplicationResult;
import com.objectstorage.model.VersionExternalApiInfoResult;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.exception.JwtVerificationFailureException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.jwt.JsonWebToken;

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
        return ValidationSecretsApplicationResult.of(
                resourceConfigurationHelper.createJwtToken(validationSecretsApplication));
    }
}
