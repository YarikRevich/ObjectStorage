package com.objectstorage.service.integration.communication.registry;

import com.objectstorage.exception.ApplicationStartGuardFailureException;
import com.objectstorage.exception.CommunicationConfigurationFailureException;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.state.StateService;
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
 * Service used to perform initial communication infrastructure configuration.
 */
@Startup(value = 200)
@ApplicationScoped
public class RegistryCommunicationConfigService {
    @Inject
    ConfigService configService;

    /**
     * Performs initial communication infrastructure configuration.
     *
     * @throws ApplicationStartGuardFailureException      if ObjectStorage API Server application start guard operation fails.
     * @throws CommunicationConfigurationFailureException if ObjectStorage API Server communication configuration
     *                                                    operation fails.
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
            registry = LocateRegistry.createRegistry(
                    configService.getConfig().getCommunication().getPort());
        } catch (RemoteException e) {
            throw new CommunicationConfigurationFailureException(e.getMessage());
        }

        try {
            registry.list();
        } catch (RemoteException e) {
            throw new CommunicationConfigurationFailureException(e.getMessage());
        }
    }
}
