package com.objectstorage.repository.facade;

import com.objectstorage.dto.RepositoryContentLocationUnitDto;
import com.objectstorage.dto.RepositoryContentUnitDto;
import com.objectstorage.entity.repository.ContentEntity;
import com.objectstorage.entity.repository.ProviderEntity;
import com.objectstorage.entity.repository.SecretEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.repository.*;
import com.objectstorage.repository.common.RepositoryConfigurationHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.objectstorage.repository.ConfigRepository;
import com.objectstorage.repository.ContentRepository;
import com.objectstorage.repository.ProviderRepository;
import com.objectstorage.repository.SecretRepository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Represents facade for repository implementations used to handle tables.
 */
@ApplicationScoped
public class RepositoryFacade {
    @Inject
    ConfigRepository configRepository;

    @Inject
    ContentRepository contentRepository;

    @Inject
    TemporateRepository temporateRepository;

    @Inject
    ProviderRepository providerRepository;

    @Inject
    SecretRepository secretRepository;

    /**
     * Retrieves all the content for the given configuration properties.
     *
     * @param validationSecretsUnit given validation secret application unit.
     * @return retrieved content for the given configuration properties.
     * @throws ContentLocationsRetrievalFailureException if content retrieval fails.
     */
    public List<RepositoryContentLocationUnitDto> retrieveContent(ValidationSecretsUnit validationSecretsUnit)
            throws ContentLocationsRetrievalFailureException {
        ProviderEntity provider;

        try {
            provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
        } catch (RepositoryOperationFailureException e) {
            throw new ContentLocationsRetrievalFailureException(e.getMessage());
        }

        String signature = RepositoryConfigurationHelper.getExternalCredentials(
                validationSecretsUnit.getProvider(),
                validationSecretsUnit.getCredentials().getExternal());

        try {
            if (!secretRepository.isPresentBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                throw new ContentLocationsRetrievalFailureException();
            }
        } catch (RepositoryOperationFailureException e) {
            throw new ContentLocationsRetrievalFailureException(e.getMessage());
        }

        SecretEntity secret;

        try {
            secret = secretRepository.findBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(),
                    signature);
        } catch (RepositoryOperationFailureException e) {
            throw new ContentLocationsRetrievalFailureException(e.getMessage());
        }

        List<RepositoryContentLocationUnitDto> result = new ArrayList<>();

        try {
            result = contentRepository
                    .findByProviderAndSecret(provider.getId(), secret.getId())
                    .stream()
                    .map(element -> RepositoryContentLocationUnitDto.of(element.getRoot()))
                    .toList();
        } catch (RepositoryOperationFailureException ignored) {
        }

        return result;
    }

    /**
     * Retrieves all the data from content repository in a form of content applications.
     *
     * @return retrieved list of content applications.
     * @throws ContentApplicationRetrievalFailureException if content application retrieval fails.
     */
    public List<RepositoryContentUnitDto> retrieveContentApplication() throws ContentApplicationRetrievalFailureException {
        List<RepositoryContentUnitDto> result = new ArrayList<>();

        List<RepositoryContentUnitDto> units = new ArrayList<>();

        List<ContentEntity> contents;

        try {
            contents = contentRepository.findAll();
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        for (ContentEntity content : contents) {
            ProviderEntity rawProvider;

            try {
                rawProvider = providerRepository.findById(content.getProvider());
            } catch (RepositoryOperationFailureException e) {
                throw new ContentApplicationRetrievalFailureException(e.getMessage());
            }

            Provider provider =
                    RepositoryConfigurationHelper.convertRawProviderToContentProvider(
                            rawProvider.getName());

            SecretEntity rawSecret;

            try {
                rawSecret = secretRepository.findById(content.getSecret());
            } catch (RepositoryOperationFailureException e) {
                throw new ContentApplicationRetrievalFailureException(e.getMessage());
            }

            CredentialsFieldsFull credentials =
                    RepositoryConfigurationHelper.convertRawSecretsToContentCredentials(
                            provider, rawSecret.getSession(), rawSecret.getCredentials());

            units.add(RepositoryContentUnitDto.of(
                    content.getRoot(),
                    provider,
                    credentials));
        }

        Map<CredentialsFieldsFull, Map<Provider, List<RepositoryContentUnitDto>>> groups =
                units
                        .stream()
                        .collect(
                                groupingBy(
                                        RepositoryContentUnitDto::getCredentials,
                                        groupingBy(RepositoryContentUnitDto::getProvider)));

        groups
                .forEach((key1, value1) -> value1
                        .forEach((key2, value2) -> value2.forEach(
                                value3 -> result.add(
                                        RepositoryContentUnitDto.of(
                                                value3.getRoot(),
                                                key2,
                                                key1)))));

        return result;
    }

    /**
     * Applies given content application.
     *
     * @param contentApplication given content application used for topology configuration.
     * @param validationSecretsApplication given validation secrets application.
     * @throws RepositoryContentApplicationFailureException if ObjectStorage repository content application failed.
     */
    public void apply(
            ContentApplication contentApplication, ValidationSecretsApplication validationSecretsApplication)
            throws RepositoryContentApplicationFailureException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            ProviderEntity provider;

            try {
                provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            String signature = RepositoryConfigurationHelper.getExternalCredentials(
                    validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials().getExternal());

            try {
                if (secretRepository.isPresentBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                    throw new RepositoryContentApplicationFailureException(
                            new RepositoryContentApplicationExistsException().getMessage());
                }
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            try {
                secretRepository.insert(
                        validationSecretsUnit.getCredentials().getInternal().getId(),
                        signature);
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            SecretEntity secret;

            try {
                secret = secretRepository.findBySessionAndCredentials(
                        validationSecretsUnit.getCredentials().getInternal().getId(),
                        signature);
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            try {
                contentRepository.insert(provider.getId(), secret.getId(), contentApplication.getRoot());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }
        }
    }

    /**
     * Applies given content withdrawal, removing previous state.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @throws RepositoryContentDestructionFailureException if repository content destruction failed.
     */
    public void destroy(ValidationSecretsApplication validationSecretsApplication) throws RepositoryContentDestructionFailureException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            String signature = RepositoryConfigurationHelper.getExternalCredentials(
                    validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials().getExternal());
            ProviderEntity provider;

            try {
                provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
            } catch (RepositoryOperationFailureException e) {
                return;
            }

            SecretEntity secret;

            try {
                secret = secretRepository.findBySessionAndCredentials(
                        validationSecretsUnit.getCredentials().getInternal().getId(),
                        signature);
            } catch (RepositoryOperationFailureException e) {
                return;
            }

            try {
                contentRepository.deleteByProviderAndSecret(provider.getId(), secret.getId());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentDestructionFailureException(e.getMessage());
            }

            try {
                secretRepository.deleteById(secret.getId());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentDestructionFailureException(e.getMessage());
            }
        }
    }

    /**
     * Applies given content application, updating previous state.
     *
     * @param location given object location.
     * @param hash given object hash.
     * @param validationSecretsApplication given validation secrets application.
     * @throws RepositoryContentApplicationFailureException if ObjectStorage repository content application failed.
     */
    public void upload(String location, String hash, ValidationSecretsApplication validationSecretsApplication)
            throws RepositoryContentApplicationFailureException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            ProviderEntity provider;

            try {
                provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            String signature = RepositoryConfigurationHelper.getExternalCredentials(
                    validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials().getExternal());

            try {
                if (!secretRepository.isPresentBySessionAndCredentials(
                        validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                    secretRepository.insert(
                            validationSecretsUnit.getCredentials().getInternal().getId(),
                            signature);
                }
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            SecretEntity secret;

            try {
                secret = secretRepository.findBySessionAndCredentials(
                        validationSecretsUnit.getCredentials().getInternal().getId(),
                        signature);
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            try {
                temporateRepository.insert(provider.getId(), secret.getId(), location, hash);
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }
        }
    }
}
