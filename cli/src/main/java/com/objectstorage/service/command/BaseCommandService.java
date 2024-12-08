package com.objectstorage.service.command;

import com.objectstorage.dto.CleanExternalCommandDto;
import com.objectstorage.dto.DownloadBackupExternalCommandDto;
import com.objectstorage.dto.DownloadObjectExternalCommandDto;
import com.objectstorage.dto.UploadObjectExternalCommandDto;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.service.command.external.apply.ApplyExternalCommandService;
import com.objectstorage.service.command.external.clean.object.CleanObjectExternalCommandService;
import com.objectstorage.service.command.external.cleanall.CleanAllExternalCommandService;
import com.objectstorage.service.command.external.content.ContentExternalCommandService;
import com.objectstorage.service.command.external.download.backup.DownloadBackupExternalCommandService;
import com.objectstorage.service.command.external.download.object.DownloadObjectExternalCommandService;
import com.objectstorage.service.command.external.upload.object.UploadObjectExternalCommandService;
import com.objectstorage.service.command.external.version.VersionExternalCommandService;
import com.objectstorage.service.command.external.withdraw.WithdrawExternalCommandService;
import com.objectstorage.service.command.internal.health.HealthCheckInternalCommandService;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.visualization.VisualizationService;
import com.objectstorage.service.visualization.label.apply.ApplyCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.clean.CleanCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.cleanall.CleanAllCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.content.ContentCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.download.object.DownloadObjectCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.withdraw.WithdrawCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.version.VersionCommandVisualizationLabel;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Objects;

/**
 * Represents general command management service.
 */
@Service
@Command(
        name = "help",
        mixinStandardHelpOptions = true,
        description = "Repository achieving tool",
        version = "1.0")
public class BaseCommandService {
    private static final Logger logger = LogManager.getLogger(BaseCommandService.class);

    @Autowired
    private PropertiesEntity properties;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ApplyExternalCommandService applyExternalCommandService;

    @Autowired
    private WithdrawExternalCommandService withdrawExternalCommandService;

    @Autowired
    private CleanObjectExternalCommandService cleanExternalCommandService;

    @Autowired
    private CleanAllExternalCommandService cleanAllExternalCommandService;

    @Autowired
    private ContentExternalCommandService contentExternalCommandService;

    @Autowired
    private DownloadObjectExternalCommandService downloadObjectExternalCommandService;

    @Autowired
    private DownloadBackupExternalCommandService downloadBackupExternalCommandService;

    @Autowired
    private UploadObjectExternalCommandService uploadObjectExternalCommandService;

    @Autowired
    private VersionExternalCommandService versionExternalCommandService;

    @Autowired
    private HealthCheckInternalCommandService healthCheckInternalCommandService;

    @Autowired
    private ApplyCommandVisualizationLabel applyCommandVisualizationLabel;

    @Autowired
    private WithdrawCommandVisualizationLabel withdrawCommandVisualizationLabel;

    @Autowired
    private CleanCommandVisualizationLabel cleanCommandVisualizationLabel;

    @Autowired
    private CleanAllCommandVisualizationLabel cleanAllCommandVisualizationLabel;

    @Autowired
    private ContentCommandVisualizationLabel contentCommandVisualizationLabel;

    @Autowired
    private DownloadObjectCommandVisualizationLabel downloadCommandVisualizationLabel;

    @Autowired
    private VersionCommandVisualizationLabel versionCommandVisualizationLabel;

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    private VisualizationState visualizationState;

    /**
     * Provides access to apply command service.
     *
     * @param configLocation given custom configuration file location.
     */
    @Command(description = "Apply remote configuration")
    private void apply(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(applyCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            applyExternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to withdraw command service.
     *
     * @param configLocation given custom configuration file location.
     */
    @Command(description = "Withdraw remote configuration")
    private void withdraw(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(withdrawCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            withdrawExternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to clean command service.
     *
     * @param configLocation given custom configuration file location.
     * @param location given remote content location name.
     */
    @Command(description = "Clean remote content")
    private void clean(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation,
            @Option(names = {"--location"}, description = "A name of remote content location", required = true)
            String location) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(cleanCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            cleanExternalCommandService.process(CleanExternalCommandDto.of(configService.getConfig(), location));
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to cleanall command service.
     *
     * @param configLocation given custom configuration file location.
     */
    @Command(description = "Clean all remote content")
    private void cleanAll(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(cleanAllCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            cleanAllExternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to content command service.
     *
     * @param configLocation given custom configuration file location.
     */
    @Command(description = "Retrieve remote content state")
    private void content(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(contentCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            contentExternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to upload object command service.
     *
     * @param configLocation given custom configuration file location.
     * @param location given remote content object location name.
     * @param file given input file location.
     */
    @Command(description = "Upload selected content object")
    private void uploadObject(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation,
            @Option(names = {"--location"}, description = "A name of remote content location", required = true)
            String location,
            @Option(names = {"--file"}, description = "A path for the file to be upload", required = true)
            String file) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(downloadCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            uploadObjectExternalCommandService.process(UploadObjectExternalCommandDto.of(
                    configService.getConfig(),
                    location,
                    file));
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to download object command service.
     *
     * @param configLocation given custom configuration file location.
     * @param provider given selected provider name.
     * @param outputLocation given output file location.
     * @param location given remote content object location name.
     */
    @Command(description = "Download selected content object")
    private void downloadObject(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation,
            @Option(names = {"--provider"}, description = "A name of selected provider", required = true) String provider,
            @Option(names = {"--output"}, description = "A path for the file to be downloaded", required = true) String outputLocation,
            @Option(names = {"--location"}, description = "A name of remote content location", required = true)
            String location) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(downloadCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            downloadObjectExternalCommandService.process(DownloadObjectExternalCommandDto.of(
                    configService.getConfig(),
                    provider,
                    outputLocation,
                    location));
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to download backup command service.
     *
     * @param configLocation given custom configuration file location.
     * @param outputLocation given output file location.
     * @param location given remote content backup location name.
     */
    @Command(description = "Download selected content backup")
    private void downloadBackup(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation,
            @Option(names = {"--output"}, description = "A path for the file to be downloaded", required = true) String outputLocation,
            @Option(names = {"--location"}, description = "A name of remote content location", required = true)
            String location) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(downloadCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        try {
            downloadBackupExternalCommandService.process(DownloadBackupExternalCommandDto.of(
                    configService.getConfig(),
                    outputLocation,
                    location));
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        visualizationService.await();
    }

    /**
     * Provides access to version command service.
     *
     * @param configLocation given custom configuration file location.
     */
    @Command(description = "Retrieve versions of the infrastructure)")
    private void version(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation) {
        if (Objects.equals(configLocation, "null")) {
            configLocation = properties.getConfigDefaultLocation();
        }

        visualizationState.setLabel(versionCommandVisualizationLabel);

        visualizationService.process();

        try {
            configService.configure(configLocation);
        } catch (ConfigFileNotFoundException | ConfigFileReadingFailureException | ConfigValidationException |
                 ConfigFileClosureFailureException e) {
            logger.fatal(new ApiServerOperationFailureException(e.getMessage()).getMessage());

            return;
        }

        try {
            healthCheckInternalCommandService.process(configService.getConfig());
        } catch (ApiServerOperationFailureException e) {
            logger.fatal(e.getMessage());

            return;
        }

        versionExternalCommandService.process(configService.getConfig());

        visualizationService.await();
    }
}
