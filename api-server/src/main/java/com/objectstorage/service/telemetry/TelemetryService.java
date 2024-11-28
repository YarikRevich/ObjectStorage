package com.objectstorage.service.telemetry;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.telemetry.binding.TelemetryBinding;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides access to gather information and expose it to telemetry representation tool.
 */
@ApplicationScoped
public class TelemetryService {
    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    TelemetryBinding telemetryBinding;

    private final ConcurrentLinkedQueue<Runnable> temporateStorageFilesAmountQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedQueue<Runnable> currentCloudServiceUploadsQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedQueue<Runnable> cloudServiceUploadRetriesQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedQueue<Runnable> averageUploadFileSizeQueue = new ConcurrentLinkedQueue<>();

    private final static ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());

    /**
     * Starts telemetries listener, which handles incoming telemetries to be processed in a sequential way.
     */
    @PostConstruct
    private void configure() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!temporateStorageFilesAmountQueue.isEmpty()) {
                temporateStorageFilesAmountQueue.poll().run();
            }
        }, 0, properties.getDiagnosticsScrapeDelay(), TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!currentCloudServiceUploadsQueue.isEmpty()) {
                currentCloudServiceUploadsQueue.poll().run();
            }
        }, 0, properties.getDiagnosticsScrapeDelay(), TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!cloudServiceUploadRetriesQueue.isEmpty()) {
                cloudServiceUploadRetriesQueue.poll().run();
            }
        }, 0, properties.getDiagnosticsScrapeDelay(), TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!averageUploadFileSizeQueue.isEmpty()) {
                averageUploadFileSizeQueue.poll().run();
            }
        }, 0, properties.getDiagnosticsScrapeDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sets current amount of files in ObjectStorage Temporate Storage.
     *
     * @param value given value.
     */
    public void setTemporateStorageFilesAmount(Integer value) {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            temporateStorageFilesAmountQueue.add(
                    () -> telemetryBinding.getTemporateStorageFilesAmount().set(value));
        }
    }

    /**
     * Increases current cloud service uploads from ObjectStorage Temporate Storage.
     */
    public void increaseCurrentCloudServiceUploads() {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            currentCloudServiceUploadsQueue.add(
                    () -> telemetryBinding.getCurrentCloudServiceUploadsAmount().set(
                            telemetryBinding.getCurrentCloudServiceUploadsAmount().get() + 1));
        }
    }

    /**
     * Increases cloud service upload retries form ObjectStorage Temporate Storage.
     */
    public void increaseCloudServiceUploadRetries() {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            cloudServiceUploadRetriesQueue.add(
                    () -> telemetryBinding.getCloudServiceUploadRetries().set(
                            telemetryBinding.getCloudServiceUploadRetries().get() + 1));
        }
    }

    /**
     * Sets average upload file size to ObjectStorage Temporate Storage.
     *
     * @param value given value.
     */
    public void setAverageUploadFileSizeQueue(Double value) {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            averageUploadFileSizeQueue.add(
                    () -> telemetryBinding.getAverageUploadFileSize().set(value));
        }
    }
}
