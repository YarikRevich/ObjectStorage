package com.objectstorage.entity;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/** Represents application properties used for application configuration. */
@Getter
@Configuration
public class PropertiesEntity {
  private static final String GIT_CONFIG_PROPERTIES_FILE = "git.properties";

  @Value(value = "${git.commit.id.abbrev}")
  private String gitCommitId;

  @Value(value = "${rest-client.timeout}")
  private String restClientTimeout;

  @Value(value = "${config.default.location}")
  private String configDefaultLocation;

  @Value(value = "${progress.visualization.period}")
  private Integer progressVisualizationPeriod;

  @Value(value = "${progress.visualization.apply-request}")
  private String progressVisualizationApplyRequestLabel;

  @Value(value = "${progress.visualization.apply-response}")
  private String progressVisualizationApplyResponseLabel;

  @Value(value = "${progress.visualization.withdraw-request}")
  private String progressVisualizationWithdrawRequestLabel;

  @Value(value = "${progress.visualization.withdraw-response}")
  private String progressVisualizationWithdrawResponseLabel;

  @Value(value = "${progress.visualization.clean-object-request}")
  private String progressVisualizationCleanObjectRequestLabel;

  @Value(value = "${progress.visualization.clean-object-response}")
  private String progressVisualizationCleanObjectResponseLabel;

  @Value(value = "${progress.visualization.clean-all-request}")
  private String progressVisualizationCleanAllRequestLabel;

  @Value(value = "${progress.visualization.clean-all-response}")
  private String progressVisualizationCleanAllResponseLabel;

  @Value(value = "${progress.visualization.content-request}")
  private String progressVisualizationContentRequestLabel;

  @Value(value = "${progress.visualization.content-response}")
  private String progressVisualizationContentResponseLabel;

  @Value(value = "${progress.visualization.download-object-request}")
  private String progressVisualizationDownloadObjectRequestLabel;

  @Value(value = "${progress.visualization.download-object-response}")
  private String progressVisualizationDownloadObjectResponseLabel;

  @Value(value = "${progress.visualization.download-backup-request}")
  private String progressVisualizationDownloadBackupRequestLabel;

  @Value(value = "${progress.visualization.download-backup-response}")
  private String progressVisualizationDownloadBackupResponseLabel;

  @Value(value = "${progress.visualization.upload-object-request}")
  private String progressVisualizationUploadObjectRequestLabel;

  @Value(value = "${progress.visualization.upload-object-response}")
  private String progressVisualizationUploadObjectResponseLabel;

  @Value(value = "${progress.visualization.version-request}")
  private String progressVisualizationVersionRequestLabel;

  @Value(value = "${progress.visualization.version-response}")
  private String progressVisualizationVersionResponseLabel;

  @Value(value = "${progress.visualization.health-check-request}")
  private String progressVisualizationHealthCheckRequestLabel;

  @Value(value = "${progress.visualization.health-check-response}")
  private String progressVisualizationHealthCheckResponseLabel;

  @Value(value = "${progress.visualization.secrets-acquire-request}")
  private String progressVisualizationSecretsAcquireRequestLabel;

  @Value(value = "${progress.visualization.secrets-acquire-response}")
  private String progressVisualizationSecretsAcquireResponseLabel;

  @Value(value = "${logging.state.frequency}")
  private Integer loggingStateFrequency;

  @Bean
  private static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
    propsConfig.setLocation(new ClassPathResource(GIT_CONFIG_PROPERTIES_FILE));
    propsConfig.setIgnoreResourceNotFound(true);
    propsConfig.setIgnoreUnresolvablePlaceholders(true);
    return propsConfig;
  }

  /**
   * Removes the last symbol in git commit id of the repository.
   *
   * @return chopped repository git commit id.
   */
  public String getGitCommitId() {
    return StringUtils.chop(gitCommitId);
  }
}
