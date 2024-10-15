package com.objectstorage.service.integration.communication.apiserver;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.ApplicationStartGuardFailureException;
import com.objectstorage.exception.CommunicationConfigurationFailureException;
import com.objectstorage.resource.communication.ApiServerCommunicationResource;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.communication.common.CommunicationProviderConfigurationHelper;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.telemetry.TelemetryService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Service used to perform ObjectStorage API Server communication provider configuration.
 */
@Startup(value = 300)
@ApplicationScoped
public class ApiServerCommunicationConfigService {
    private static final Logger logger = LogManager.getLogger(ApiServerCommunicationConfigService.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    ApiServerCommunicationResource apiServerCommunicationResource;

    /**
     * Performs setup of ObjectStorage API Server communication provider.
     *
     * @throws ApplicationStartGuardFailureException      if ObjectStorage API Server application start guard operation
     *                                                    fails.
     * @throws CommunicationConfigurationFailureException if ObjectStorage API Server communication configuration fails.
     */
    @PostConstruct
    private void process() throws
            ApplicationStartGuardFailureException,
            CommunicationConfigurationFailureException {
        try {
            StateService.getStartGuard().await();
        } catch (InterruptedException e) {
            throw new ApplicationStartGuardFailureException(e.getMessage());
        }

        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(
                    configService.getConfig().getCommunication().getPort());
        } catch (RemoteException e) {
            throw new CommunicationConfigurationFailureException(e.getMessage());
        }

        Thread.ofPlatform().start(() -> {
            try {
                registry.rebind(
                        CommunicationProviderConfigurationHelper.getBindName(
                                configService.getConfig().getCommunication().getPort(),
                                properties.getCommunicationApiServerName()),
                        apiServerCommunicationResource);
            } catch (RemoteException e) {
                logger.fatal(e.getMessage());
            }
        });
    }
}