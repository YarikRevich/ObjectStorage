package com.objectstorage.service.communication.apiserver;

import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.InputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Represents communication provider for ObjectStorage API Server.
 */
public interface IApiServerCommunicationService extends Remote {
    /**
     * Performs raw content upload operation at the given location with the given name, initiated by ObjectStorage Cluster.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param location         given content location.
     * @param name             given content name.
     * @param content          given content to be uploaded.
     * @throws RemoteException if remote request fails.
     */
    void performRawContentUpload(String workspaceUnitKey, String location, String name, RemoteInputStream content)
            throws RemoteException;

    /**
     * Checks if raw content with the given value at the given location is already present.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param location         given content location.
     * @param value            given content name.
     * @return result of the check.
     * @throws RemoteException if remote request fails
     */
    Boolean retrieveRawContentPresent(String workspaceUnitKey, String location, String value) throws RemoteException;

    /**
     * Performs additional content(issues, prs, releases) upload operation at the given location with the given name,
     * initiated by ObjectStorage Cluster.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param location         given content location.
     * @param name             given content name.
     * @param data             given converted content data to be uploaded.
     * @throws RemoteException if remote request fails.
     */
    void performAdditionalContentUpload(String workspaceUnitKey, String location, String name, String data)
            throws RemoteException;

    /**
     * Checks if additional content with the given value at the given location is already present.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param location         given content location.
     * @param value            given content name.
     * @return result of the check.
     * @throws RemoteException if remote request fails
     */
    Boolean retrieveAdditionalContentPresent(String workspaceUnitKey, String location, String value)
            throws RemoteException;

    /**
     * Handles incoming log messages related to the given ObjectStorage Cluster allocation.
     *
     * @param name    given ObjectStorage Cluster allocation name.
     * @param message given ObjectStorage Cluster log message.
     * @throws RemoteException if remote request fails.
     */
    void performLogsTransfer(String name, String message) throws RemoteException;

    /**
     * Handles incoming download telemetry amount increase.
     *
     * @throws RemoteException if remote request fails.
     */
    void performDownloadTelemetryIncrease() throws RemoteException;

    /**
     * Handles incoming download telemetry amount decrease.
     *
     * @throws RemoteException if remote request fails.
     */
    void performDownloadTelemetryDecrease() throws RemoteException;

    /**
     * Handles ObjectStorage Cluster allocation lock.
     *
     * @param name given ObjectStorage Cluster allocation name.
     * @throws RemoteException if remote request fails.
     */
    void performLockClusterAllocation(String name) throws RemoteException;

    /**
     * Retrieves ObjectStorage Cluster allocation lock state.
     *
     * @param name given ObjectStorage Cluster allocation name.
     * @return ObjectStorage Cluster allocation lock state.
     * @throws RemoteException if remote request fails.
     */
    Boolean retrieveClusterAllocationLocked(String name) throws RemoteException;

    /**
     * Retrieves latest ObjectStorage API Server health check states.
     *
     * @return ObjectStorage API Server health check status.
     * @throws RemoteException if remote request fails.
     */
    Boolean retrieveHealthCheck() throws RemoteException;
}