package com.objectstorage.service.state.watcher;

import lombok.Getter;
import lombok.Setter;

/**
 * Service used to track state metrics for the current session in the application.
 */
public class WatcherService {

    /**
     * Represents amount of files uploaded to ObjectStorage Temporate Storage in the current session.
     */
    @Getter
    @Setter
    private static Double filesUploadCounter = (double) 0;

    /**
     * Represents global amount of files content uploaded in the current session.
     */
    @Getter
    @Setter
    private static Double uploadedFilesSize = (double) 0;
}
