package com.objectstorage.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Represents configuration model used for ObjectStorage API Server operations.
 */
@Getter
@ApplicationScoped
public class ConfigEntity {
    /**
     * Represents ObjectStorage API Server configuration used for ObjectStorage API Server instance setup.
     */
    @Getter
    public static class Connection {
        @NotNull
        @JsonProperty("port")
        public Integer port;
    }

    @Valid
    @NotNull
    @JsonProperty("connection")
    public Connection connection;

    /**
     * Represents ObjectStorage API Server configuration used for internal communication infrastructure setup.
     */
    @Getter
    public static class Communication {
        @NotNull
        @JsonProperty("port")
        public Integer port;
    }

    @Valid
    @NotNull
    @JsonProperty("communication")
    public Communication communication;

    /**
     * Represents ObjectStorage API Server configuration used for content management.
     */
    @Getter
    public static class Content {
        /**
         * Represents all supported content formats, which can be used by ObjectStorage Cluster allocation.
         */
        @Getter
        public enum Format {
            @JsonProperty("zip")
            ZIP("zip"),

            @JsonProperty("tar")
            TAR("tar");

            private final String value;

            Format(String value) {
                this.value = value;
            }

            public String toString() {
                return value;
            }
        }

        @Valid
        @NotNull
        @JsonProperty("format")
        public Format format;
    }

    @Valid
    @NotNull
    @JsonProperty("content")
    public Content content;

    /**
     * Represents ObjectStorage API Server configuration used for diagnostics.
     */
    @Getter
    public static class Diagnostics {
        @NotNull
        @JsonProperty("enabled")
        public Boolean enabled;

        /**
         * Represents ObjectStorage API Server metrics configuration setup.
         */
        @Getter
        public static class Metrics {
            @NotNull
            @JsonProperty("port")
            public Integer port;
        }

        @Valid
        @NotNull
        @JsonProperty("metrics")
        public Metrics metrics;

        /**
         * Represents ObjectStorage API Server configuration used for Grafana instance setup.
         */
        @Getter
        public static class Grafana {
            @NotNull
            @JsonProperty("port")
            public Integer port;
        }

        @Valid
        @NotNull
        @JsonProperty("grafana")
        public Grafana grafana;

        /**
         * Represents ObjectStorage API Server configuration used for Prometheus instance setup.
         */
        @Getter
        public static class Prometheus {
            @NotNull
            @JsonProperty("port")
            public Integer port;
        }

        @Valid
        @NotNull
        @JsonProperty("prometheus")
        public Prometheus prometheus;

        /**
         * Represents ObjectStorage API Server configuration used for Prometheus Node Exporter instance setup.
         */
        @Getter
        public static class NodeExporter {
            @NotNull
            @JsonProperty("port")
            public Integer port;
        }

        @Valid
        @NotNull
        @JsonProperty("node-exporter")
        public NodeExporter nodeExporter;
    }

    @Valid
    @NotNull
    @JsonProperty("diagnostics")
    public Diagnostics diagnostics;

    /**
     * Represents ObjectStorage API Server resources configuration section.
     */
    @Getter
    public static class Resource {
        /**
         * Represents ObjectStorage API Server configuration used for ObjectStorage Cluster.
         */
        @Getter
        public static class Cluster {
            @NotNull
            @Min(1)
            @JsonProperty("max-workers")
            public Integer maxWorkers;

            @NotNull
            @Min(1)
            @JsonProperty("max-versions")
            public Integer maxVersions;
        }

        @Valid
        @NotNull
        @JsonProperty("cluster")
        public Cluster cluster;

        /**
         * Represents ObjectStorage API Server configuration used for ObjectStorage Worker.
         */
        @Getter
        public static class Worker {
            @NotNull
            @JsonProperty("frequency")
            public String frequency;
        }

        @Valid
        @NotNull
        @JsonProperty("worker")
        public Worker worker;
    }

    @Valid
    @NotNull
    @JsonProperty("resource")
    public Resource resource;
}