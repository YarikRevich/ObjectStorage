package com.objectstorage.service.state.watcher;

import org.apache.commons.io.FileUtils;

/**
 * Service used to track state metrics for the current session in the application.
 */
public class WatcherService {
    /**
     * Represents amount of files uploaded to ObjectStorage Temporate Storage in the current session.
     */
    private Integer filesUploadCounter = 0;

    /**
     * Increases amount of files uploaded to ObjectStorage Temporate Storage in the current session.
     */
    public void increaseFilesUploadCounter() {
        filesUploadCounter++;
    }

    /**
     * Represents global files size uploaded in the current session.
     */
    private Integer uploadedFilesSize = 0;

    /**
     * Increases global files size uploaded in the current session with the given value.
     *
     * @param value given value of uploaded file size.
     */
    public void increaseUploadedFilesSize(Integer value) {
        uploadedFilesSize += value;
    }

    /**
     * Calculates average file size in the current session.
     *
     * @return calculated average file size.
     */
    public Double getAverageFileSize() {
        if (filesUploadCounter > 0) {
            return Double.valueOf(uploadedFilesSize) / Double.valueOf(filesUploadCounter) / 1024 / 1024;
        }

        return (double) 0;
    }
}
