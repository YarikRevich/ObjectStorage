package com.objectstorage.service.state;

import com.objectstorage.service.state.watcher.WatcherService;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Service used to operate as a collection of application state properties.
 */
public class StateService {
    /**
     * Represents ObjectStorage API Server JWT secret, which is automatically generated during application startup.
     */
    @Getter
    @Setter
    private static String jwtSecretKey = "";

    /**
     * Represents ObjectStorage Temporate Storage processor guard.
     */
    @Getter
    private final static ReentrantLock temporateStorageProcessorGuard = new ReentrantLock();

    /**
     * Represents ObjectStorage Backup processor guard.
     */
    @Getter
    private final static ReentrantLock backupProcessorGuard = new ReentrantLock();

    /**
     * Represents ObjectStorage transaction guard.
     */
    @Getter
    private final static ReentrantLock transactionProcessorGuard = new ReentrantLock();

    /**
     * Represents ObjectStorage watcher service instance.
     */
    @Getter
    private static WatcherService watcherService;
}
