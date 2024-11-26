package com.objectstorage.entity.common;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Exposes access to properties setup to be used for further configuration.
 */
@Getter
@ApplicationScoped
public class PropertiesEntity {
    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    @ConfigProperty(name = "quarkus.http.host")
    String applicationHost;

    @ConfigProperty(name = "quarkus.http.port")
    Integer applicationPort;

    @ConfigProperty(name = "content.root.notation")
    String contentRootNotation;

    @ConfigProperty(name = "secrets.jwt.upn")
    String secretsJwtUpn;

    @ConfigProperty(name = "secrets.jwt.ttl")
    Integer secretsJwtTtl;

    @ConfigProperty(name = "secrets.jwt.claims.name")
    String secretsJwtClaimsName;

    @ConfigProperty(name = "secrets.jwt.header.notation")
    String secretsJwtHeaderNotation;

    @ConfigProperty(name = "database.tables.config.name")
    String databaseConfigTableName;

    @ConfigProperty(name = "database.tables.content.name")
    String databaseContentTableName;

    @ConfigProperty(name = "database.tables.temporate.name")
    String databaseTemporateTableName;

    @ConfigProperty(name = "database.tables.provider.name")
    String databaseProviderTableName;

    @ConfigProperty(name = "database.tables.secret.name")
    String databaseSecretTableName;

    @ConfigProperty(name = "database.statement.close-delay")
    Integer databaseStatementCloseDelay;

    @ConfigProperty(name = "database.transaction.savepoint.symbols.count")
    Integer databaseTransactionSavepointSymbolsCount;

    @ConfigProperty(name = "config.location")
    String configLocation;

    @ConfigProperty(name = "workspace.directory")
    String workspaceDirectory;

    @ConfigProperty(name = "workspace.content.object.directory")
    String workspaceContentObjectDirectory;

    @ConfigProperty(name = "workspace.content.backup.directory")
    String workspaceContentBackupDirectory;

    @ConfigProperty(name = "workspace.compression.file.name")
    String workspaceCompressionFileName;

    @ConfigProperty(name = "diagnostics.scrape.delay")
    Integer diagnosticsScrapeDelay;

    @ConfigProperty(name = "diagnostics.common.docker.network.name")
    String diagnosticsCommonDockerNetworkName;

    @ConfigProperty(name = "diagnostics.grafana.config.location")
    String diagnosticsGrafanaConfigLocation;

    @ConfigProperty(name = "diagnostics.grafana.datasources.location")
    String diagnosticsGrafanaDatasourcesLocation;

    @ConfigProperty(name = "diagnostics.grafana.datasources.template")
    String diagnosticsGrafanaDatasourcesTemplate;

    @ConfigProperty(name = "diagnostics.grafana.datasources.output")
    String diagnosticsGrafanaDatasourcesOutput;

    @ConfigProperty(name = "diagnostics.grafana.dashboards.location")
    String diagnosticsGrafanaDashboardsLocation;

    @ConfigProperty(name = "diagnostics.grafana.dashboards.diagnostics.template")
    String diagnosticsGrafanaDashboardsDiagnosticsTemplate;

    @ConfigProperty(name = "diagnostics.grafana.dashboards.diagnostics.output")
    String diagnosticsGrafanaDashboardsDiagnosticsOutput;

    @ConfigProperty(name = "diagnostics.grafana.internal.location")
    String diagnosticsGrafanaInternalLocation;

    @ConfigProperty(name = "diagnostics.grafana.docker.name")
    String diagnosticsGrafanaDockerName;

    @ConfigProperty(name = "diagnostics.grafana.docker.image")
    String diagnosticsGrafanaDockerImage;

    @ConfigProperty(name = "diagnostics.prometheus.config.location")
    String diagnosticsPrometheusConfigLocation;

    @ConfigProperty(name = "diagnostics.prometheus.config.template")
    String diagnosticsPrometheusConfigTemplate;

    @ConfigProperty(name = "diagnostics.prometheus.config.output")
    String diagnosticsPrometheusConfigOutput;

    @ConfigProperty(name = "diagnostics.prometheus.internal.location")
    String diagnosticsPrometheusInternalLocation;

    @ConfigProperty(name = "diagnostics.prometheus.docker.name")
    String diagnosticsPrometheusDockerName;

    @ConfigProperty(name = "diagnostics.prometheus.docker.image")
    String diagnosticsPrometheusDockerImage;

    @ConfigProperty(name = "diagnostics.prometheus.node-exporter.docker.name")
    String diagnosticsPrometheusNodeExporterDockerName;

    @ConfigProperty(name = "diagnostics.prometheus.node-exporter.docker.image")
    String diagnosticsPrometheusNodeExporterDockerImage;

    @ConfigProperty(name = "diagnostics.metrics.connection.timeout")
    Integer diagnosticsMetricsConnectionTimeout;

    @ConfigProperty(name = "git.commit.id.abbrev")
    String gitCommitId;

    /**
     * Removes the last symbol in git commit id of the repository.
     *
     * @return chopped repository git commit id.
     */
    public String getGitCommitId() {
        return StringUtils.chop(gitCommitId);
    }
}
