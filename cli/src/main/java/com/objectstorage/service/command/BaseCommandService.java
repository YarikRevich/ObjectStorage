package com.objectstorage.service.command;

import com.objectstorage.dto.CleanExternalCommandDto;
import com.objectstorage.dto.DownloadExternalCommandDto;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.service.command.external.apply.ApplyExternalCommandService;
import com.objectstorage.service.command.external.clean.CleanExternalCommandService;
import com.objectstorage.service.command.external.cleanall.CleanAllExternalCommandService;
import com.objectstorage.service.command.external.content.ContentExternalCommandService;
import com.objectstorage.service.command.external.download.DownloadExternalCommandService;
import com.objectstorage.service.command.external.version.VersionExternalCommandService;
import com.objectstorage.service.command.external.withdraw.WithdrawExternalCommandService;
import com.objectstorage.service.command.internal.health.HealthCheckInternalCommandService;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.visualization.VisualizationService;
import com.objectstorage.service.visualization.label.apply.ApplyCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.clean.CleanCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.cleanall.CleanAllCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.content.ContentCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.download.DownloadCommandVisualizationLabel;
import com.objectstorage.service.visualization.label.topology.TopologyCommandVisualizationLabel;
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
    private CleanExternalCommandService cleanExternalCommandService;

    @Autowired
    private CleanAllExternalCommandService cleanAllExternalCommandService;

    @Autowired
    private ContentExternalCommandService contentExternalCommandService;

    @Autowired
    private DownloadExternalCommandService downloadExternalCommandService;

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
    private DownloadCommandVisualizationLabel downloadCommandVisualizationLabel;

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
     * Provides access to download command service.
     *
     * @param configLocation given custom configuration file location.
     * @param location given remote content location name.
     * @param outputLocation given output file location.
     */
    @Command(description = "Retrieve remote content state")
    private void download(
            @Option(names = {"--config"}, description = "A location of configuration file", defaultValue = "null")
            String configLocation,
            @Option(names = {"--output"}, description = "", required = true) String outputLocation,
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
            downloadExternalCommandService.process(DownloadExternalCommandDto.of(configService.getConfig(), outputLocation, location));
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
    @Command(description = "Retrieve versions of infrastructure)")
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
