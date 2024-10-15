package com.objectstorage.service.cluster.resource;

import com.objectstorage.exception.ClusterOperationFailureException;
import com.objectstorage.exception.CommunicationConfigurationFailureException;
import com.objectstorage.service.communication.common.CommunicationProviderConfigurationHelper;
import com.objectstorage.service.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import com.objectstorage.service.communication.cluster.IClusterCommunicationService;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Represents implementation for ObjectStorage Cluster remote API.
 */
@ApplicationScoped
public class ClusterCommunicationResource {
    @Inject
    ConfigService configService;

    private Registry registry;

    /**
     * Prepares RMI registry configuration.
     *
     * @throws CommunicationConfigurationFailureException if communication configuration fails.
     */
    @PostConstruct
    private void configure() throws CommunicationConfigurationFailureException {
        try {
            this.registry = LocateRegistry.getRegistry(
                    configService.getConfig().getCommunication().getPort());
        } catch (RemoteException e) {
            throw new CommunicationConfigurationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves remote ObjectStorage Cluster allocation with the given name.
     *
     * @param name given ObjectStorage Cluster allocation name.
     * @return retrieved ObjectStorage Cluster allocation.
     * @throws ClusterOperationFailureException if ObjectStorage Cluster operation fails.
     */
    private IClusterCommunicationService retrieveAllocation(String name) throws ClusterOperationFailureException {
        try {
            return (IClusterCommunicationService) registry.lookup(
                    CommunicationProviderConfigurationHelper.getBindName(
                            configService.getConfig().getCommunication().getPort(),
                            name));
        } catch (RemoteException | NotBoundException e) {
            throw new ClusterOperationFailureException(e.getMessage());
        }
    }

    /**
     * Performs ObjectStorage Cluster suspend operation. Has no effect if ObjectStorage Cluster was already suspended
     * previously.
     *
     * @param name given name of ObjectStorage Cluster.
     * @throws ClusterOperationFailureException if ObjectStorage Cluster operation fails.
     */
    public void performSuspend(String name) throws ClusterOperationFailureException {
        IClusterCommunicationService allocation = retrieveAllocation(name);

        try {
            allocation.performSuspend();
        } catch (RemoteException e) {
            throw new ClusterOperationFailureException(e.getMessage());
        }
    }

    /**
     * Performs ObjectStorage Cluster serve operation. Has no effect if ObjectStorage Cluster was not suspended previously.
     *
     * @param name given name of ObjectStorage Cluster.
     * @throws ClusterOperationFailureException if ObjectStorage Cluster operation fails.
     */
    public void performServe(String name) throws ClusterOperationFailureException {
        IClusterCommunicationService allocation = retrieveAllocation(name);

        try {
            allocation.performServe();
        } catch (RemoteException e) {
            throw new ClusterOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves health check status of the ObjectStorage Cluster with the given name.
     *
     * @param name given name of ObjectStorage Cluster.
     * @return result of the check.
     * @throws ClusterOperationFailureException if ObjectStorage Cluster operation fails.
     */
    public Boolean retrieveHealthCheck(String name) throws ClusterOperationFailureException {
        IClusterCommunicationService allocation = retrieveAllocation(name);

        try {
            return allocation.retrieveHealthCheck();
        } catch (RemoteException e) {
            throw new ClusterOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves version of the ObjectStorage Cluster with the given name.
     *
     * @param name given name of ObjectStorage Cluster.
     * @return retrieved version of ObjectStorage Cluster.
     * @throws ClusterOperationFailureException if ObjectStorage Cluster operation fails.
     */
    public String retrieveVersion(String name) throws ClusterOperationFailureException {
        IClusterCommunicationService allocation = retrieveAllocation(name);

        try {
            return allocation.retrieveVersion();
        } catch (RemoteException e) {
            throw new ClusterOperationFailureException(e.getMessage());
        }
    }
}
