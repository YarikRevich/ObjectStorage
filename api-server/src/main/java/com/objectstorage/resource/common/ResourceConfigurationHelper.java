package com.objectstorage.resource.common;

import com.objectstorage.dto.SecretsCacheDto;
import com.objectstorage.dto.ValidationSecretsResultDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.SecretsConversionException;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsUnit;
import com.objectstorage.service.state.StateService;
import com.objectstorage.exception.JwtVerificationFailureException;
import com.objectstorage.exception.TimeLimitedCacheKeyNotFoundException;
import com.objectstorage.service.secrets.cache.TimeLimitedCacheService;
import com.objectstorage.service.vendor.VendorFacade;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
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
    public SecretsCacheDto getJwtDetails(String rawToken) throws JwtVerificationFailureException {
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

     * @param secretsCacheDto given secrets cache dto to be used as token claim.
     * @return created new jwt token with the help of internally created jwt secret key.
     */
    public String createJwtToken(SecretsCacheDto secretsCacheDto) {
        String key = timeLimitedCacheService.add(secretsCacheDto);

        return Jwt
                .upn(properties.getSecretsJwtUpn())
                .claim(properties.getSecretsJwtClaimsName(), key)
                .expiresIn(Duration.ofMillis(properties.getSecretsJwtTtl()))
                .signWithSecret(StateService.getJwtSecretKey());
    }

    /**
     * Validates provided secrets application and converts to secrets result dto.
     *
     * @param validationSecretsApplication given validation secrets application to be validated.
     * @return converted secrets result dto
     * @throws SecretsConversionException if secrets conversion fails or provided secrets are invalid.
     */
    public List<ValidationSecretsResultDto> validateSecretsApplication(
            ValidationSecretsApplication validationSecretsApplication) throws SecretsConversionException {
        List<ValidationSecretsResultDto> validationSecretsResultDtoList = new ArrayList<>();

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            ValidationSecretsResultDto validationSecretsResultDto =
                    vendorFacade.areExternalCredentialsValid(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal());

            if (!validationSecretsResultDto.getValid()) {
                throw new SecretsConversionException();
            }

            validationSecretsResultDtoList.add(validationSecretsResultDto);
        }

        return validationSecretsResultDtoList;
    }

    /**
     * Checks if the given locations have duplicates.
     *
     * @return result of the check.
     */
    public Boolean areProvidersDuplicated(List<String> d) {
//        return locations.stream().distinct().count() == locations.size();
        return true;
    }

//    /**
//     * Checks if the given external credentials field is valid according to the used provider.
//     *
//     * @param provider given vendor provider.
//     * @param credentialsFieldExternal given credentials field.
//     * @return result of the check.
//     */
//    public Boolean isExternalCredentialsFieldValid(
//            Provider provider, CredentialsFieldsExternal credentialsFieldExternal) {
//        return switch (provider) {
//            case S3 -> Objects.nonNull(credentialsFieldExternal);
//        };
//    }
}
