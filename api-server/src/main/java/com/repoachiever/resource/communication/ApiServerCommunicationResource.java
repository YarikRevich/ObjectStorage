package com.objectstorage.resource.communication;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.objectstorage.converter.JsonToAdditionalContentDataConverter;
import com.objectstorage.entity.common.AdditionalContentFileEntity;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.AdditionalContentCreationFailureException;
import com.objectstorage.exception.AdditionalContentRetrievalFailureException;
import com.objectstorage.exception.RawContentCreationFailureException;
import com.objectstorage.exception.RawContentRetrievalFailureException;
import com.objectstorage.service.communication.apiserver.IApiServerCommunicationService;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.integration.diagnostics.DiagnosticsConfigService;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.telemetry.TelemetryService;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Contains implementation of communication provider for ObjectStorage API Server.
 */
@ApplicationScoped
public class ApiServerCommunicationResource extends UnicastRemoteObject implements IApiServerCommunicationService {
    private static final Logger logger = LogManager.getLogger(ApiServerCommunicationResource.class);

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    TelemetryService telemetryService;

    public ApiServerCommunicationResource() throws RemoteException {
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performRawContentUpload(
            String workspaceUnitKey, String location, String name, RemoteInputStream content) throws RemoteException {
        telemetryService.increaseRawContentUploadAmount();

        StateService.getCommunicationGuard().lock();

        InputStream contentRaw;

        try {
            contentRaw = RemoteInputStreamClient.wrap(content);
        } catch (IOException e) {
            StateService.getCommunicationGuard().unlock();

            telemetryService.decreaseRawContentUploadAmount();

            throw new RuntimeException(e);
        }

        try {
            workspaceFacade.addRawContent(workspaceUnitKey, location, name, contentRaw);
        } catch (RawContentCreationFailureException e) {
            StateService.getCommunicationGuard().unlock();

            telemetryService.decreaseRawContentUploadAmount();

            throw new RemoteException(e.getMessage());
        }

        StateService.getCommunicationGuard().unlock();

        telemetryService.decreaseRawContentUploadAmount();
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public Boolean retrieveRawContentPresent(String workspaceUnitKey, String location, String value)
            throws RemoteException {
        StateService.getCommunicationGuard().lock();

        Boolean result;

        try {
            result = workspaceFacade.isRawContentPresent(workspaceUnitKey, location, value);
        } catch (RawContentRetrievalFailureException e) {
            StateService.getCommunicationGuard().unlock();

            throw new RemoteException(e.getMessage());
        }

        StateService.getCommunicationGuard().unlock();

        return result;
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performAdditionalContentUpload(
            String workspaceUnitKey, String location, String name, String data) throws RemoteException {
        telemetryService.increaseAdditionalContentUploadAmount();

        StateService.getCommunicationGuard().lock();

        Map<String, String> rawData = JsonToAdditionalContentDataConverter.convert(data);
        if (Objects.isNull(rawData)) {
            StateService.getCommunicationGuard().unlock();

            telemetryService.decreaseAdditionalContentUploadAmount();

            throw new RemoteException();
        }

        try {
            workspaceFacade.addAdditionalContent(
                    workspaceUnitKey, location, name, AdditionalContentFileEntity.of(
                            rawData
                                    .entrySet()
                                    .stream()
                                    .map(
                                            element -> AdditionalContentFileEntity.Data.of(element.getKey(), element.getValue()))
                                    .toList()));
        } catch (AdditionalContentCreationFailureException e) {
            StateService.getCommunicationGuard().unlock();

            telemetryService.decreaseAdditionalContentUploadAmount();

            throw new RemoteException(e.getMessage());
        }

        StateService.getCommunicationGuard().unlock();

        telemetryService.decreaseAdditionalContentUploadAmount();
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public Boolean retrieveAdditionalContentPresent(String workspaceUnitKey, String location, String value)
            throws RemoteException {
        StateService.getCommunicationGuard().lock();

        Boolean result;

        try {
            result = workspaceFacade.isAdditionalContentPresent(workspaceUnitKey, location, value);
        } catch (AdditionalContentRetrievalFailureException e) {
            StateService.getCommunicationGuard().unlock();

            throw new RemoteException(e.getMessage());
        }

        StateService.getCommunicationGuard().unlock();

        return result;
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performLogsTransfer(String name, String message) throws RemoteException {
        logger.info(String.format("Transferred logs(instance: %s): %s", name, message));
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performDownloadTelemetryIncrease() throws RemoteException {
        telemetryService.increaseClusterDownloadAmount();
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performDownloadTelemetryDecrease() throws RemoteException {
        telemetryService.decreaseClusterDownloadAmount();
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public void performLockClusterAllocation(String name) throws RemoteException {
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public Boolean retrieveClusterAllocationLocked(String name) throws RemoteException {
        return false;
    }

    /**
     * @see IApiServerCommunicationService
     */
    @Override
    public Boolean retrieveHealthCheck() throws RemoteException {
        telemetryService.increaseApiServerHealthCheckAmount();

        Boolean result = true;

        telemetryService.decreaseApiServerHealthCheckAmount();

        return result;
    }
}
