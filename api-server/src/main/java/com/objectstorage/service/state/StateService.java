package com.objectstorage.service.state;

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
}
