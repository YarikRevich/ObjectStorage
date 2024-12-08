package com.objectstorage;

import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.service.command.BaseCommandService;
import com.objectstorage.service.command.external.clean.CleanExternalCommandService;
import com.objectstorage.service.command.external.cleanall.CleanAllExternalCommandService;
import com.objectstorage.service.command.external.content.ContentExternalCommandService;
import com.objectstorage.service.command.external.download.DownloadExternalCommandService;
import com.objectstorage.service.command.external.apply.ApplyExternalCommandService;
import com.objectstorage.service.command.external.withdraw.WithdrawExternalCommandService;
import com.objectstorage.service.command.external.version.VersionExternalCommandService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * Represents initialization point for the ObjectStorage CLI application.
 */
@Component
@Import({
        BaseCommandService.class,
        ApplyExternalCommandService.class,
        WithdrawExternalCommandService.class,
        CleanExternalCommandService.class,
        CleanAllExternalCommandService.class,
        ContentExternalCommandService.class,
        DownloadExternalCommandService.class,
        VersionExternalCommandService.class,
        VersionExternalCommandService.class,
        HealthCheckInternalCommandService.class,
        ApplyExternalCommandService.class,
        WithdrawExternalCommandService.class,
        BuildProperties.class,
        PropertiesEntity.class,
        ConfigService.class,
        ApplyCommandVisualizationLabel.class,
        WithdrawCommandVisualizationLabel.class,
        CleanCommandVisualizationLabel.class,
        CleanAllCommandVisualizationLabel.class,
        ContentCommandVisualizationLabel.class,
        DownloadCommandVisualizationLabel.class,
        TopologyCommandVisualizationLabel.class,
        VersionCommandVisualizationLabel.class,
        VisualizationService.class,
        VisualizationState.class
})
public class App implements ApplicationRunner, ExitCodeGenerator {
    private int exitCode;

    @Autowired
    private BaseCommandService baseCommandService;

    @Override
    public void run(ApplicationArguments args) {
        CommandLine cmd = new CommandLine(baseCommandService);

        exitCode = cmd.execute(args.getSourceArgs());
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
