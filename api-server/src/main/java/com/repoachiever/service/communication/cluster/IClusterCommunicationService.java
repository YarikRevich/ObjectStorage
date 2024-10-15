package com.objectstorage.service.communication.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Represents client for ObjectStorage Cluster remote API. */
public interface IClusterCommunicationService extends Remote {
    /**
     * Performs ObjectStorage Cluster suspend operation. Has no effect if ObjectStorage Cluster was
     * already suspended previously.
     *
     * @throws RemoteException if remote request fails.
     */
    void performSuspend() throws RemoteException;

    /**
     * Performs ObjectStorage Cluster serve operation. Has no effect if ObjectStorage Cluster was not
     * suspended previously.
     *
     * @throws RemoteException if remote request fails.
     */
    void performServe() throws RemoteException;

    /**
     * Retrieves latest ObjectStorage Cluster health check states.
     *
     * @return ObjectStorage Cluster health check status.
     * @throws RemoteException if remote request fails.
     */
    Boolean retrieveHealthCheck() throws RemoteException;

    /**
     * Retrieves version of the allocated ObjectStorage Cluster instance allowing to confirm API
     * compatability.
     *
     * @return ObjectStorage Cluster version.
     * @throws RemoteException if remote request fails.
     */
    String retrieveVersion() throws RemoteException;
}