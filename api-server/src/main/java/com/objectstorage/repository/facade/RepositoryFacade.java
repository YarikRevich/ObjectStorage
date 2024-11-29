package com.objectstorage.repository.facade;

import com.objectstorage.dto.*;
import com.objectstorage.entity.repository.ContentEntity;
import com.objectstorage.entity.repository.ProviderEntity;
import com.objectstorage.entity.repository.SecretEntity;
import com.objectstorage.entity.repository.TemporateEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.repository.*;
import com.objectstorage.repository.common.RepositoryConfigurationHelper;
import com.objectstorage.service.telemetry.TelemetryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.objectstorage.repository.ContentRepository;
import com.objectstorage.repository.ProviderRepository;
import com.objectstorage.repository.SecretRepository;

import java.time.Instant;
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

    @Inject
    TelemetryService telemetryService;

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
                element -> ContentRetrievalProviderUnit.of(
                        element.getLocation(), element.getCreatedAt())).toList();
    }

    /**
     * Checks if there are any available temporate content from temporate repository.
     *
     * @return result of the check.
     * @throws TemporateContentRetrievalFailureException if temporate content amount retrieval fails.
     */
    public Boolean isTemporateContentPresent() throws TemporateContentRetrievalFailureException {
        Integer amount;

        try {
            amount = temporateRepository.count();
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRetrievalFailureException(e.getMessage());
        }

        telemetryService.setTemporateStorageFilesAmount(amount);

        return amount > 0;
    }

    /**
     * Retrieves the earliest content from temporate repository.
     *
     * @return retrieved earliest temporate content.
     * @throws TemporateContentRetrievalFailureException if the earliest temporate content retrieval fails.
     */
    public EarliestTemporateContentDto retrieveEarliestTemporateContent() throws TemporateContentRetrievalFailureException {
        TemporateEntity temporateEntity;

        try {
            temporateEntity = temporateRepository.findEarliest();
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRetrievalFailureException(e.getMessage());
        }

        if (Objects.isNull(temporateEntity)) {
            throw new TemporateContentRetrievalFailureException();
        }

        List<TemporateEntity> temporateEntities;

        try {
            temporateEntities = temporateRepository.findByHash(temporateEntity.getHash());
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRetrievalFailureException(e.getMessage());
        }

        List<ContentCompoundUnitDto>  contentCompoundUnits = new ArrayList<>();

        for (TemporateEntity temporate : temporateEntities) {
            ProviderEntity rawProvider;

            try {
                rawProvider = providerRepository.findById(temporate.getProvider());
            } catch (RepositoryOperationFailureException e) {
                throw new TemporateContentRetrievalFailureException(e.getMessage());
            }

            Provider provider =
                    repositoryConfigurationHelper.convertRawProviderToContentProvider(rawProvider.getName());

            SecretEntity secret;

            try {
                secret = secretRepository.findById(temporate.getSecret());
            } catch (RepositoryOperationFailureException e) {
                throw new TemporateContentRetrievalFailureException(e.getMessage());
            }

            CredentialsFieldsFull credentials =
                    repositoryConfigurationHelper.convertRawSecretsToContentCredentials(
                            provider,
                            secret.getSession(),
                            secret.getCredentials());

            ContentEntity contentEntity;

            try {
                contentEntity = contentRepository.findByProviderAndSecret(rawProvider.getId(), secret.getId());
            } catch (RepositoryOperationFailureException e) {
                throw new TemporateContentRetrievalFailureException(e.getMessage());
            }

            contentCompoundUnits.add(
                    ContentCompoundUnitDto.of(
                            RepositoryContentUnitDto.of(
                                    contentEntity.getRoot()),
                            provider,
                            credentials));
        }

        return EarliestTemporateContentDto.of(
                contentCompoundUnits,
                temporateEntity.getLocation(),
                temporateEntity.getHash(),
                temporateEntity.getCreatedAt());
    }

    /**
     * Retrieves temporate content from the temporate repository with the given location, provider and secret.
     *
     * @param location given temporate content location.
     * @param validationSecretsUnit given validation secrets unit.
     * @return retrieved temporate content.
     */
    public TemporateContentUnitDto retrieveTemporateContentByLocationProviderAndSecret(String location, ValidationSecretsUnit validationSecretsUnit)
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

        TemporateEntity temporate;

        try {
            temporate = temporateRepository
                    .findEarliestByLocationProviderAndSecret(location, provider.getId(), secret.getId());
        } catch (RepositoryOperationFailureException ignored) {
            return null;
        }

        return TemporateContentUnitDto.of(
                temporate.getProvider(),
                temporate.getSecret(),
                temporate.getLocation(),
                temporate.getHash(),
                temporate.getCreatedAt());
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

        ContentEntity contentEntity;

        try {
             contentEntity = contentRepository
                    .findByProviderAndSecret(provider.getId(), secret.getId());
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        return RepositoryContentUnitDto.of(contentEntity.getRoot());
    }

    /**
     * Retrieves all content applications from the content repository.
     *
     * @return retrieved all content applications.
     * @throws ContentApplicationRetrievalFailureException if content applications retrieval fails.
     */
    public List<RepositoryContentApplicationUnitDto> retrieveAllContentApplications()
            throws ContentApplicationRetrievalFailureException {
        List<ContentEntity> contentEntities;

        try {
            contentEntities = contentRepository.findAll();
        } catch (RepositoryOperationFailureException e) {
            throw new ContentApplicationRetrievalFailureException(e.getMessage());
        }

        List<RepositoryContentApplicationUnitDto> repositoryContentApplicationUnits = new ArrayList<>();

        for (ContentEntity content : contentEntities) {
            ProviderEntity rawProvider;

            try {
                rawProvider = providerRepository.findById(content.getProvider());
            } catch (RepositoryOperationFailureException e) {
                throw new ContentApplicationRetrievalFailureException(e.getMessage());
            }

            Provider provider =
                    repositoryConfigurationHelper.convertRawProviderToContentProvider(rawProvider.getName());

            SecretEntity secret;

            try {
                secret = secretRepository.findById(content.getSecret());
            } catch (RepositoryOperationFailureException e) {
                throw new ContentApplicationRetrievalFailureException(e.getMessage());
            }

            CredentialsFieldsFull credentials =
                    repositoryConfigurationHelper.convertRawSecretsToContentCredentials(
                            provider,
                            secret.getSession(),
                            secret.getCredentials());

            repositoryContentApplicationUnits.add(
                    RepositoryContentApplicationUnitDto.of(
                            content.getRoot(), provider, credentials));
        }

        return repositoryContentApplicationUnits;
    }

    /**
     * Removes temporate content from the temporate repository with the given hash.
     *
     * @param hash given temporate content hash.
     * @throws TemporateContentRemovalFailureException if temporate content removal fails.
     */
    public void removeTemporateContentByHash(String hash)
            throws TemporateContentRemovalFailureException {
        try {
            temporateRepository.deleteByHash(hash);
        } catch (RepositoryOperationFailureException e) {
            throw new TemporateContentRemovalFailureException(e.getMessage());
        }
    }

    /**
     * Removes temporate content from the temporate repository with the given location, provider and secret.
     *
     * @param location given temporate content location.
     * @param validationSecretsUnit given validation secrets unit.
     */
    public void removeTemporateContentByLocationProviderAndSecret(String location, ValidationSecretsUnit validationSecretsUnit)
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
     * Removes temporate content from the temporate repository with the given provider and secret.
     *
     * @param validationSecretsUnit given validation secrets unit.
     * @throws TemporateContentRemovalFailureException if temporate content removal fails.
     */
    public void removeTemporateContentByProviderAndSecret(ValidationSecretsUnit validationSecretsUnit)
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
            temporateRepository.deleteByProviderAndSecret(provider.getId(), secret.getId());
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
                temporateRepository.insert(
                        provider.getId(), secret.getId(), location, hash, Instant.now().getEpochSecond());
            } catch (RepositoryOperationFailureException e) {
                throw new RepositoryContentApplicationFailureException(e.getMessage());
            }
    }
}
