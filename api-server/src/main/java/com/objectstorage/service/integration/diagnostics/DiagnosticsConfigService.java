package com.objectstorage.service.integration.diagnostics;

import com.objectstorage.dto.CommandExecutorOutputDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.service.command.docker.availability.DockerAvailabilityCheckCommandService;
import com.objectstorage.service.command.docker.inspect.remove.DockerInspectRemoveCommandService;
import com.objectstorage.service.command.docker.network.create.DockerNetworkCreateCommandService;
import com.objectstorage.service.command.docker.network.remove.DockerNetworkRemoveCommandService;
import com.objectstorage.service.command.grafana.GrafanaDeployCommandService;
import com.objectstorage.service.command.nodeexporter.NodeExporterDeployCommandService;
import com.objectstorage.service.command.prometheus.PrometheusDeployCommandService;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.executor.CommandExecutorService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Service used to perform diagnostics infrastructure configuration operations.
 */
@Startup(value = 700)
@ApplicationScoped
public class DiagnosticsConfigService {
    private static final Logger logger = LogManager.getLogger(DiagnosticsConfigService.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    CommandExecutorService commandExecutorService;

    @Inject
    DockerAvailabilityCheckCommandService dockerAvailabilityCheckCommandService;

    /**
     * Creates Docker diagnostics network and deploys diagnostics infrastructure instances with pre-defined configurations.
     *
     * @throws DockerIsNotAvailableException if Docker is not available.
     * @throws DockerInspectRemovalFailureException if Docker container removal operation fails.
     * @throws DockerNetworkCreateFailureException if Docker network creation operation fails.
     * @throws NodeExporterDeploymentFailureException if Prometheus Node Exporter deployment operation fails.
     * @throws PrometheusDeploymentFailureException if Prometheus deployment operation fails.
     * @throws GrafanaDeploymentFailureException if Grafana deployment operation fails.
     */
    @PostConstruct
    private void process() throws
            DockerIsNotAvailableException,
            DockerInspectRemovalFailureException,
            DockerNetworkCreateFailureException,
            NodeExporterDeploymentFailureException,
            PrometheusDeploymentFailureException,
            GrafanaDeploymentFailureException {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            CommandExecutorOutputDto dockerAvailabilityCommandOutput;

            try {
                dockerAvailabilityCommandOutput =
                        commandExecutorService.executeCommand(dockerAvailabilityCheckCommandService);
            } catch (CommandExecutorException e) {
                throw new DockerIsNotAvailableException(e.getMessage());
            }

            String dockerAvailabilityCommandErrorOutput = dockerAvailabilityCommandOutput.getErrorOutput();

            if ((Objects.nonNull(dockerAvailabilityCommandErrorOutput) &&
                    !dockerAvailabilityCommandErrorOutput.isEmpty()) ||
                    dockerAvailabilityCommandOutput.getNormalOutput().isEmpty()) {
                throw new DockerIsNotAvailableException(dockerAvailabilityCommandErrorOutput);
            }

            DockerInspectRemoveCommandService dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsPrometheusDockerName());

            CommandExecutorOutputDto dockerInspectRemoveCommandOutput;

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                throw new DockerInspectRemovalFailureException(e.getMessage());
            }

            String dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                throw new DockerInspectRemovalFailureException(
                        dockerInspectRemoveCommandErrorOutput);
            }

            dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsPrometheusNodeExporterDockerName());

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                throw new DockerInspectRemovalFailureException(e.getMessage());
            }

            dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                throw new DockerInspectRemovalFailureException(dockerInspectRemoveCommandErrorOutput);
            }

            dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsGrafanaDockerName());

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                throw new DockerInspectRemovalFailureException(e.getMessage());
            }

            dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                throw new DockerInspectRemovalFailureException(dockerInspectRemoveCommandErrorOutput);
            }

            DockerNetworkCreateCommandService dockerNetworkCreateCommandService =
                    new DockerNetworkCreateCommandService(properties.getDiagnosticsCommonDockerNetworkName());

            CommandExecutorOutputDto dockerNetworkCreateCommandOutput;

            try {
                dockerNetworkCreateCommandOutput =
                        commandExecutorService.executeCommand(dockerNetworkCreateCommandService);
            } catch (CommandExecutorException e) {
                throw new DockerNetworkCreateFailureException(e.getMessage());
            }

            String dockerNetworkCreateCommandErrorOutput = dockerNetworkCreateCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerNetworkCreateCommandErrorOutput) &&
                    !dockerNetworkCreateCommandErrorOutput.isEmpty()) {
                throw new DockerNetworkCreateFailureException(dockerNetworkCreateCommandErrorOutput);
            }

            NodeExporterDeployCommandService nodeExporterDeployCommandService =
                    new NodeExporterDeployCommandService(
                            properties.getDiagnosticsPrometheusNodeExporterDockerName(),
                            properties.getDiagnosticsPrometheusNodeExporterDockerImage(),
                            configService.getConfig().getDiagnostics().getNodeExporter().getPort(),
                            properties.getDiagnosticsCommonDockerNetworkName());

            try {
                commandExecutorService.executeCommand(nodeExporterDeployCommandService);
            } catch (CommandExecutorException e) {
                throw new NodeExporterDeploymentFailureException(e.getMessage());
            }

            PrometheusDeployCommandService prometheusDeployCommandService =
                    new PrometheusDeployCommandService(
                            properties.getDiagnosticsPrometheusDockerName(),
                            properties.getDiagnosticsPrometheusDockerImage(),
                            configService.getConfig().getDiagnostics().getPrometheus().getPort(),
                            properties.getDiagnosticsCommonDockerNetworkName(),
                            properties.getDiagnosticsPrometheusConfigLocation(),
                            properties.getDiagnosticsPrometheusInternalLocation());

            try {
                 commandExecutorService.executeCommand(prometheusDeployCommandService);
            } catch (CommandExecutorException e) {
                throw new PrometheusDeploymentFailureException(e.getMessage());
            }

            GrafanaDeployCommandService grafanaDeployCommandService =
                    new GrafanaDeployCommandService(
                            properties.getDiagnosticsGrafanaDockerName(),
                            properties.getDiagnosticsGrafanaDockerImage(),
                            configService.getConfig().getDiagnostics().getGrafana().getPort(),
                            properties.getDiagnosticsCommonDockerNetworkName(),
                            properties.getDiagnosticsGrafanaConfigLocation(),
                            properties.getDiagnosticsGrafanaInternalLocation());

            try {
                commandExecutorService.executeCommand(grafanaDeployCommandService);
            } catch (CommandExecutorException e) {
                throw new GrafanaDeploymentFailureException(e.getMessage());
            }
        }
    }

    /**
     * Removes created Docker networks and stops started diagnostics containers.
     */
    @PreDestroy
    private void close() {
        if (configService.getConfig().getDiagnostics().getEnabled()) {
            CommandExecutorOutputDto dockerAvailabilityCommandOutput;

            try {
                dockerAvailabilityCommandOutput =
                        commandExecutorService.executeCommand(dockerAvailabilityCheckCommandService);
            } catch (CommandExecutorException e) {
                return;
            }

            String dockerAvailabilityCommandErrorOutput = dockerAvailabilityCommandOutput.getErrorOutput();

            if ((Objects.nonNull(dockerAvailabilityCommandErrorOutput) &&
                    !dockerAvailabilityCommandErrorOutput.isEmpty()) ||
                    dockerAvailabilityCommandOutput.getNormalOutput().isEmpty()) {
                return;
            }

            DockerNetworkRemoveCommandService dockerNetworkRemoveCommandService =
                    new DockerNetworkRemoveCommandService(properties.getDiagnosticsCommonDockerNetworkName());

            CommandExecutorOutputDto dockerNetworkRemoveCommandOutput;

            try {
                dockerNetworkRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerNetworkRemoveCommandService);
            } catch (CommandExecutorException e) {
                logger.fatal(new DockerNetworkRemoveFailureException(e.getMessage()).getMessage());
                return;
            }

            String dockerNetworkRemoveCommandErrorOutput = dockerNetworkRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerNetworkRemoveCommandErrorOutput) &&
                    !dockerNetworkRemoveCommandErrorOutput.isEmpty()) {
                logger.fatal(new DockerNetworkRemoveFailureException(dockerNetworkRemoveCommandErrorOutput).getMessage());
                return;
            }

            DockerInspectRemoveCommandService dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsPrometheusDockerName());

            CommandExecutorOutputDto dockerInspectRemoveCommandOutput;

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                logger.fatal(new DockerInspectRemovalFailureException(e.getMessage()).getMessage());
                return;
            }

            String dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                logger.fatal(new DockerInspectRemovalFailureException(
                        dockerInspectRemoveCommandErrorOutput).getMessage());
                return;
            }

            dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsPrometheusNodeExporterDockerName());

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                logger.fatal(new DockerInspectRemovalFailureException(e.getMessage()).getMessage());
                return;
            }

            dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                logger.fatal(new DockerInspectRemovalFailureException(
                        dockerInspectRemoveCommandErrorOutput).getMessage());
                return;
            }

            dockerInspectRemoveCommandService =
                    new DockerInspectRemoveCommandService(properties.getDiagnosticsGrafanaDockerName());

            try {
                dockerInspectRemoveCommandOutput =
                        commandExecutorService.executeCommand(dockerInspectRemoveCommandService);
            } catch (CommandExecutorException e) {
                logger.fatal(new DockerInspectRemovalFailureException(e.getMessage()).getMessage());
                return;
            }

            dockerInspectRemoveCommandErrorOutput = dockerInspectRemoveCommandOutput.getErrorOutput();

            if (Objects.nonNull(dockerInspectRemoveCommandErrorOutput) &&
                    !dockerInspectRemoveCommandErrorOutput.isEmpty()) {
                logger.fatal(new DockerInspectRemovalFailureException(
                        dockerInspectRemoveCommandErrorOutput).getMessage());
            }
        }
    }
}
