package com.objectstorage.service.telemetry.binding;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service used to create custom telemetry bindings used to distribute application metrics.
 */
@Getter
@ApplicationScoped
public class TelemetryBinding implements MeterBinder {
    private final AtomicInteger temporateStorageFilesAmount = new AtomicInteger();

    private final AtomicInteger currentCloudServiceUploadsAmount = new AtomicInteger();

    private final AtomicInteger cloudServiceUploadRetries = new AtomicInteger();

    private final AtomicInteger configuredTemporateStorageAwaitTime = new AtomicInteger();

    private final AtomicInteger averageUploadFileSize = new AtomicInteger();

    /**
     * @see MeterBinder
     */
    @Override
    public void bindTo(@NotNull MeterRegistry meterRegistry) {
        Gauge.builder("general.temporate_storage_files_amount", temporateStorageFilesAmount, AtomicInteger::get)
                .description("Represents amount of files in ObjectStorage Temporate Storage")
                .register(meterRegistry);

        Gauge.builder("general.current_cloud_service_uploads_amount", currentCloudServiceUploadsAmount, AtomicInteger::get)
                .description("Represents amount of uploads to different cloud services")
                .register(meterRegistry);

        Gauge.builder("general.cloud_service_upload_retries", cloudServiceUploadRetries, AtomicInteger::get)
                .description("Represents cloud service uploads retries from ObjectStorage Temporate Storage")
                .register(meterRegistry);

        Gauge.builder("general.configured_temporate_storage_await_time", configuredTemporateStorageAwaitTime, AtomicInteger::get)
                .description("Represents configured ObjectStorage Temporate Storage await time")
                .register(meterRegistry);

        Gauge.builder("general.average_upload_file_size", averageUploadFileSize, AtomicInteger::get)
                .description("Represents average upload file size in ObjectStorage Temporate Storage")
                .register(meterRegistry);
    }
}
