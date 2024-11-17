package com.objectstorage.resource.common;

import com.objectstorage.dto.SecretsCacheDto;
import com.objectstorage.dto.ValidationSecretsCompoundDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.SecretsConversionException;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsUnit;
import com.objectstorage.model.Provider;
import com.objectstorage.service.state.StateService;
import com.objectstorage.exception.JwtVerificationFailureException;
import com.objectstorage.exception.TimeLimitedCacheKeyNotFoundException;
import com.objectstorage.service.secrets.cache.TimeLimitedCacheService;
import com.objectstorage.service.vendor.VendorFacade;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains helpful tools used for resource configuration.
 */
@ApplicationScoped
public class ResourceConfigurationHelper {
    @Inject
    PropertiesEntity properties;

    @Inject
    JWTParser jwtParser;

    @Inject
    TimeLimitedCacheService timeLimitedCacheService;

    @Inject
    VendorFacade vendorFacade;

    /**
     * Extracts jwt details from the given raw jwt token.
     *
     * @param rawToken provided raw jwt token.
     * @return retrieved jwt token details.
     * @throws JwtVerificationFailureException if jwt token verification fails.
     */
    public ValidationSecretsApplication getJwtDetails(String rawToken) throws JwtVerificationFailureException {
        Pattern pattern = Pattern.compile(properties.getSecretsJwtHeaderNotation());

        Matcher matcher = pattern.matcher(rawToken);

        if (!matcher.find()) {
            throw new JwtVerificationFailureException();
        }

        JsonWebToken token;

        try {
            token = jwtParser
                    .verify(matcher.group(1), StateService.getJwtSecretKey());
        } catch (ParseException e) {
            throw new JwtVerificationFailureException(e.getMessage());
        }

        try {
            return timeLimitedCacheService.get(
                    token.getClaim(properties.getSecretsJwtClaimsName()));
        } catch (TimeLimitedCacheKeyNotFoundException e) {
            throw new JwtVerificationFailureException(e.getMessage());
        }
    }

    /**
     * Create new jwt token with the help of internally created jwt secret key.

     * @param validationSecretsApplication given secrets cache dto to be used as token claim.
     * @return created new jwt token with the help of internally created jwt secret key.
     */
    public String createJwtToken(ValidationSecretsApplication validationSecretsApplication) {
        String key = timeLimitedCacheService.add(validationSecretsApplication);

        return Jwt
                .upn(properties.getSecretsJwtUpn())
                .claim(properties.getSecretsJwtClaimsName(), key)
                .expiresIn(Duration.ofMillis(properties.getSecretsJwtTtl()))
                .signWithSecret(StateService.getJwtSecretKey());
    }

    /**
     * Converts and validates given external credentials according to the selected provider type.
     *
     * @param validationSecretsApplication given validation secrets application to be validated.
     * @return result of the check.
     * @throws SecretsConversionException if secrets conversion fails or provided secrets are invalid.
     */
    public Boolean areSecretsValid(
            ValidationSecretsApplication validationSecretsApplication) throws SecretsConversionException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            if (!vendorFacade.areCredentialsValid(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given providers have duplicates.
     *
     * @param providers given providers to be checked.
     * @return result of the check.
     */
    public Boolean areProvidersDuplicated(List<Provider> providers) {
        return providers.stream().distinct().count() == providers.size();
    }

    /**
     * Checks if the given root definition is valid.
     *
     * @param root given root to be validated.
     * @return result of the check.
     */
    public Boolean isRootDefinitionValid(String root) {
        return root.matches(properties.getContentRootNotation());
    }
}
