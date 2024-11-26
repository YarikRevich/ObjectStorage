package com.objectstorage.repository.facade;

import com.objectstorage.dto.RepositoryContentUnitDto;
import com.objectstorage.dto.RepositoryTemporateUnitDto;
import com.objectstorage.entity.repository.ProviderEntity;
import com.objectstorage.entity.repository.SecretEntity;
import com.objectstorage.entity.repository.TemporateEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.repository.*;
import com.objectstorage.repository.common.RepositoryConfigurationHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.objectstorage.repository.ContentRepository;
import com.objectstorage.repository.ProviderRepository;
import com.objectstorage.repository.SecretRepository;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Represents facade for repository implementations used to handle tables.
 */
@ApplicationScoped
public class RepositoryFacade {
    @Inject
    RepositoryConfigurationHelper repositoryConfigurationHelper;

    @Inject
    ContentRepository contentRepository;

    @Inject
    TemporateRepository temporateRepository;

    @Inject
    ProviderRepository providerRepository;

    @Inject
    SecretRepository secretRepository;

    /**
     * Retrieves filtered content from temporate repository.
     *
     * @param validationSecretsUnit given validation secrets unit.
     * @return retrieved list of filtered temporate content.
     * @throws TemporateContentRetrievalFailureException if filtered temporate content retrieval fails.
     */
    public List<ContentRetrievalProviderUnit> retrieveFilteredTemporateContent(
            ValidationSecretsUnit validationSecretsUnit) throws TemporateContentRetrievalFailureException {
        ProviderEntity rawProvider;

        try {
            rawProvider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRetrievalFailureException(e.getMessage());
        }

        String signature = repositoryConfigurationHelper.getExternalCredentials(
                validationSecretsUnit.getProvider(),
                validationSecretsUnit.getCredentials().getExternal());

        SecretEntity secret;

        try {
            secret = secretRepository.findBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(),
                    signature);
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRetrievalFailureException(e.getMessage());
        }

        List<TemporateEntity> temporateContent = new ArrayList<>();

        try {
            temporateContent = temporateRepository.findByProviderAndSecret(rawProvider.getId(), secret.getId());
        } catch (RepositoryOperationFailureException ignored) {
        }

        return temporateContent.stream().map(
                element -> ContentRetrievalProviderUnit.of(element.getLocation())).toList();
    }

    /**
     * Retrieves content application from the content repository.
     *
     * @param validationSecretsUnit given validation secret application unit.
     * @return retrieved content application for the given configuration properties.
     * @throws ContentApplicationRetrievalFailureException if content application retrieval fails.
     */
    public RepositoryContentUnitDto retrieveContentApplication(ValidationSecretsUnit validationSecretsUnit)
            throws ContentApplicationRetrievalFailureException {
        ProviderEntity provider;

        try {
            provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        String signature = repositoryConfigurationHelper.getExternalCredentials(
                validationSecretsUnit.getProvider(),
                validationSecretsUnit.getCredentials().getExternal());

        try {
            if (!secretRepository.isPresentBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                throw new ContentApplicationRetrievalFailureException(
                        new RepositoryContentApplicationNotExistsException().getMessage());
            }
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        SecretEntity secret;

        try {
            secret = secretRepository.findBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(),
                    signature);
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        try {
            return contentRepository
                    .findByProviderAndSecret(provider.getId(), secret.getId())
                    .stream()
                    .map(element -> RepositoryContentUnitDto.of(element.getRoot()))
                    .toList().getFirst();
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes temporate content from the temporate repository.
     *
     * @param location given temporate content location.
     * @param validationSecretsUnit given validation secrets unit.
     */
    public void removeTemporateContent(String location, ValidationSecretsUnit validationSecretsUnit)
            throws TemporateContentRemovalFailureException {
        ProviderEntity provider;

        try {
            provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRemovalFailureException(e.getMessage());
        }

        String signature = repositoryConfigurationHelper.getExternalCredentials(
                validationSecretsUnit.getProvider(),
                validationSecretsUnit.getCredentials().getExternal());

        try {
            if (!secretRepository.isPresentBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                throw new TemporateContentRemovalFailureException(
                        new RepositoryContentApplicationNotExistsException().getMessage());
            }
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRemovalFailureException(e.getMessage());
        }

        SecretEntity secret;

        try {
            secret = secretRepository.findBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(),
                    signature);
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRemovalFailureException(e.getMessage());
        }

        try {
            temporateRepository.deleteByLocationProviderAndSecret(location, provider.getId(), secret.getId());
        } catch (RepositoryOperationFailureException ignored) {
        }
    }

    /**
     * Applies given content application.
     *
     * @param contentApplication given content application used for topology configuration.
     * @param validationSecretsUnit given validation secrets unit.
     * @throws RepositoryContentApplicationFailureException if ObjectStorage repository content application failed.
     */
    public void apply(
            ContentApplication contentApplication, ValidationSecretsUnit validationSecretsUnit)
            throws RepositoryContentApplicationFailureException {
            ProviderEntity provider;

            try {
                provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            String signature = repositoryConfigurationHelper.getExternalCredentials(
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

    /**
     * Applies given content withdrawal, removing previous state.
     *
     * @param validationSecretsUnit given validation secrets unit.
     * @throws RepositoryContentDestructionFailureException if repository content destruction failed.
     */
    public void withdraw(ValidationSecretsUnit validationSecretsUnit) throws RepositoryContentDestructionFailureException {
            String signature = repositoryConfigurationHelper.getExternalCredentials(
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

    /**
     * Applies given temporate upload application.
     *
     * @param location given object location.
     * @param hash given object hash.
     * @param validationSecretsUnit given validation secrets unit.
     * @throws RepositoryContentApplicationFailureException if ObjectStorage repository content application failed.
     */
    public void upload(String location, String hash, ValidationSecretsUnit validationSecretsUnit)
            throws RepositoryContentApplicationFailureException {
            ProviderEntity provider;

            try {
                provider = providerRepository.findByName(validationSecretsUnit.getProvider().toString());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }

            String signature = repositoryConfigurationHelper.getExternalCredentials(
                    validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials().getExternal());

            try {
                if (!secretRepository.isPresentBySessionAndCredentials(
                        validationSecretsUnit.getCredentials().getInternal().getId(), signature)) {
                    throw new RepositoryContentApplicationFailureException(
                            new RepositoryContentApplicationNotExistsException().getMessage());
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
