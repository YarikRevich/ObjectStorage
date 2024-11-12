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
    ProviderRepository providerRepository;

    @Inject
    SecretRepository secretRepository;
//
//    /**
//     * Retrieves all the available locations for the given configuration properties.
//     *
//     * @param contentRetrievalApplication given content retrieval application.
//     * @return retrieved locations for the given configuration properties.
//     * @throws ContentLocationsRetrievalFailureException if content locations retrieval fails.
//     */
//    public List<RepositoryContentLocationUnitDto> retrieveLocations(
//            ContentRetrievalApplication contentRetrievalApplication) throws ContentLocationsRetrievalFailureException {
//        ProviderEntity provider;
//
//        try {
//            provider = providerRepository.findByName(contentRetrievalApplication.getProvider().toString());
//        } catch (RepositoryOperationFailureException e) {
//            throw new ContentLocationsRetrievalFailureException(e.getMessage());
//        }
//
//        Optional<String> credentials = RepositoryConfigurationHelper.getExternalCredentials(
//                contentRetrievalApplication.getProvider(),
//                contentRetrievalApplication.getCredentials().getExternal());
//
//        try {
//            if (!secretRepository.isPresentBySessionAndCredentials(
//                    contentRetrievalApplication.getCredentials().getInternal().getId(), credentials)) {
//                throw new ContentLocationsRetrievalFailureException();
//            }
//        } catch (RepositoryOperationFailureException e) {
//            throw new ContentLocationsRetrievalFailureException(e.getMessage());
//        }
//
//        SecretEntity secret;
//
//        try {
//            secret = secretRepository.findBySessionAndCredentials(
//                    contentRetrievalApplication.getCredentials().getInternal().getId(),
//                    credentials);
//        } catch (RepositoryOperationFailureException e) {
//            throw new ContentLocationsRetrievalFailureException(e.getMessage());
//        }
//
//        List<RepositoryContentLocationUnitDto> result = new ArrayList<>();
//
//        try {
//            result = contentRepository
//                    .findByProviderAndSecret(provider.getId(), secret.getId())
//                    .stream()
//                    .map(element -> RepositoryContentLocationUnitDto.of(
//                            element.getLocation(), element.getAdditional()))
//                    .toList();
//        } catch (RepositoryOperationFailureException ignored) {
//        }
//
//        return result;
//    }
//
//    /**
//     * Retrieves all the data from content repository in a form of content applications.
//     *
//     * @return retrieved set of content applications.
//     * @throws ContentApplicationRetrievalFailureException if content application retrieval fails.
//     */
//    public List<ContentApplication> retrieveContentApplication() throws ContentApplicationRetrievalFailureException {
//        List<ContentApplication> result = new ArrayList<>();
//
//        List<RepositoryContentUnitDto> units = new ArrayList<>();
//
//        List<ContentEntity> contents;
//
//        try {
//            contents = contentRepository.findAll();
//        } catch (RepositoryOperationFailureException e) {
//            throw new ContentApplicationRetrievalFailureException(e.getMessage());
//        }
//
//        for (ContentEntity content : contents) {
//            ProviderEntity rawProvider;
//
//            try {
//                rawProvider = providerRepository.findById(content.getProvider());
//            } catch (RepositoryOperationFailureException e) {
//                throw new ContentApplicationRetrievalFailureException(e.getMessage());
//            }
//
//            Provider provider =
//                    RepositoryConfigurationHelper.convertRawProviderToContentProvider(
//                            rawProvider.getName());
//
//            SecretEntity rawSecret;
//
//            try {
//                rawSecret = secretRepository.findById(content.getSecret());
//            } catch (RepositoryOperationFailureException e) {
//                throw new ContentApplicationRetrievalFailureException(e.getMessage());
//            }
//
//            CredentialsFieldsFull credentials =
//                    RepositoryConfigurationHelper.convertRawSecretsToContentCredentials(
//                            provider, rawSecret.getSession(), rawSecret.getCredentials());
//
//            Optional<Exporter> exporter;
//
//            if (content.getExporter().isPresent()) {
//                ExporterEntity rawExporter;
//
//                try {
//                    rawExporter = exporterRepository.findById(content.getExporter().get());
//                } catch (RepositoryOperationFailureException e) {
//                    throw new RuntimeException(e);
//                }
//
//                exporter = Optional.of(
//                        RepositoryConfigurationHelper.convertRawExporterToContentExporter(
//                                rawExporter.getHost()));
//            } else {
//                exporter = Optional.empty();
//            }
//
//            units.add(RepositoryContentUnitDto.of(
//                    content.getLocation(),
//                    content.getAdditional(),
//                    provider,
//                    exporter,
//                    credentials));
//        }
//
//        Map<CredentialsFieldsFull, Map<Provider, Map<Optional<Exporter>, List<RepositoryContentUnitDto>>>> groups =
//                units
//                        .stream()
//                        .collect(
//                                groupingBy(
//                                        RepositoryContentUnitDto::getCredentials,
//                                        groupingBy(RepositoryContentUnitDto::getProvider,
//                                                groupingBy(RepositoryContentUnitDto::getExporter))));
//
//        groups
//                .forEach((key1, value1) -> {
//                    value1
//                            .forEach((key2, value2) -> {
//                                value2
//                                        .forEach((key3, value3) -> {
//                                            result.add(
//                                                    ContentApplication.of(
//                                                            ContentUnit.of(
//                                                                    value3
//                                                                            .stream()
//                                                                            .map(element ->
//                                                                                    LocationsUnit.of(
//                                                                                            element.getLocation(),
//                                                                                            element.getAdditional()))
//                                                                            .toList()),
//                                                            key2,
//                                                            key3.orElse(null),
//                                                            key1));
//                                        });
//                            });
//                });
//
//        return result;
//    }
//




//    /**
//     * Applies given content application, updating previous state.
//     *
//     * @param contentApplication given content application used for topology configuration.
//     * @throws RepositoryContentApplicationFailureException if ObjectStorage Cluster repository content application failed.
//     */
//    public void apply(ContentApplication contentApplication) throws RepositoryContentApplicationFailureException {
//        ProviderEntity provider;
//
//        try {
//            provider = providerRepository.findByName(contentApplication.getProvider().toString());
//        } catch (RepositoryOperationFailureException e) {
//            throw new RepositoryContentApplicationFailureException(e.getMessage());
//        }
//
//        Optional<String> credentials = RepositoryConfigurationHelper.getExternalCredentials(
//                contentApplication.getProvider(), contentApplication.getCredentials().getExternal());
//
//        try {
//            if (!secretRepository.isPresentBySessionAndCredentials(
//                    contentApplication.getCredentials().getInternal().getId(), credentials)) {
//                secretRepository.insert(
//                        contentApplication.getCredentials().getInternal().getId(),
//                        credentials);
//            }
//        } catch (RepositoryOperationFailureException e) {
//            throw new RepositoryContentApplicationFailureException(e.getMessage());
//        }
//
//        SecretEntity secret;
//
//        try {
//            secret = secretRepository.findBySessionAndCredentials(
//                    contentApplication.getCredentials().getInternal().getId(),
//                    credentials);
//        } catch (RepositoryOperationFailureException e) {
//            throw new RepositoryContentApplicationFailureException(e.getMessage());
//        }
//
//        try {
//            contentRepository.deleteBySecret(secret.getId());
//        } catch (RepositoryOperationFailureException e) {
//            throw new RepositoryContentApplicationFailureException(e.getMessage());
//        }
//
//        for (LocationsUnit location : contentApplication.getContent().getLocations()) {
//            try {
//                contentRepository.insert(
//                        location.getName(), location.getAdditional(), provider.getId(), exporter, secret.getId());
//            } catch (RepositoryOperationFailureException e) {
//                throw new RepositoryContentApplicationFailureException(e.getMessage());
//            }
//        }
//    }
//
    /**
     * Applies given content withdrawal, removing previous state.
     *
     * @param validationSecretsUnit given validation secret application unit.
     * @throws RepositoryContentDestructionFailureException if repository content destruction failed.
     */
    public void destroy(ValidationSecretsUnit validationSecretsUnit) throws RepositoryContentDestructionFailureException {
        String credentials = RepositoryConfigurationHelper.getExternalCredentials(
                validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials().getExternal());

        SecretEntity secret;

        try {
            secret = secretRepository.findBySessionAndCredentials(
                    validationSecretsUnit.getCredentials().getInternal().getId(),
                    credentials);
        } catch (RepositoryOperationFailureException e) {
            return;
        }

        try {
            contentRepository.deleteBySecret(secret.getId());
        } catch (RepositoryOperationFailureException e) {
            throw new RepositoryContentDestructionFailureException(e.getMessage());
        }
    }
}
