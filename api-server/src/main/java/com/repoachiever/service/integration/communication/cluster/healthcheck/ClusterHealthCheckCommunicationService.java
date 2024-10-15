package com.objectstorage.service.integration.communication.cluster.healthcheck;

import com.objectstorage.dto.ClusterAllocationDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.resource.communication.ApiServerCommunicationResource;
import com.objectstorage.service.cluster.ClusterService;
import com.objectstorage.service.cluster.facade.ClusterFacade;
import com.objectstorage.service.communication.common.CommunicationProviderConfigurationHelper;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.state.StateService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service used to perform ObjectStorage Cluster communication health check operations.
 */
@Startup(value = 800)
@ApplicationScoped
public class ClusterHealthCheckCommunicationService {
    private static final Logger logger = LogManager.getLogger(ClusterHealthCheckCommunicationService.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ClusterFacade clusterFacade;

    private final static ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Performs ObjectStorage Cluster communication health check operations. If ObjectStorage Cluster is not responding,
     * then it will be redeployed.
     *
     * @throws ApplicationStartGuardFailureException if ObjectStorage API Server application start guard operation
     *                                               fails.
     */
    @PostConstruct
    private void process() throws ApplicationStartGuardFailureException {
        try {
            StateService.getStartGuard().await();
        } catch (InterruptedException e) {
            throw new ApplicationStartGuardFailureException(e.getMessage());
        }

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                clusterFacade.reApplyUnhealthy();
            } catch (ClusterUnhealthyReapplicationFailureException e) {
                logger.fatal(e.getMessage());
            }
        }, 0, properties.getCommunicationClusterHealthCheckFrequency(), TimeUnit.MILLISECONDS);
    }
}