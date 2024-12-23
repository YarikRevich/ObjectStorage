package com.objectstorage.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents configuration model used for ObjectStorage API Server operations.
 */
@Getter
@ApplicationScoped
public class ConfigEntity {
    /**
     * Represents ObjectStorage API Server configuration used for ObjectStorage API
     * Server instance setup.
     */
    @Getter
    public static class Connection {
        @NotNull
        @JsonProperty("port")
        public Integer port;

        /**
         * Represents ObjectStorage API Server security configuration.
         */
        @Getter
        public static class Security {
            @NotNull
            @JsonProperty("enabled")
            public Boolean enabled;

            @NotNull
            @JsonProperty("file")
            public String file;

            @NotNull
            @JsonProperty("password")
            public String password;
        }

        @JsonProperty("security")
        public Security security;
    }

    @Valid
    @NotNull
    @JsonProperty("connection")
    public Connection connection;

    /**
     * Represents ObjectStorage internal storage configuration used for internal database setup.
     */
    @Getter
    public static class InternalStorage {
        /**
         * Represents all supported providers, which can be used by ObjectStorage internal storage.
         */
        @Getter
        public enum Provider {
            @JsonProperty("sqlite3")
            SQLITE3("sqlite3"),

            @JsonProperty("postgres")
            POSTGRES("postgres");

            private final String value;

            Provider(String value) {
                this.value = value;
            }

            public String toString() {
                return value;
            }
        }

        @Valid
        @NotNull
        @JsonProperty("provider")
        public Provider provider;

        @JsonProperty("host")
        public String host;

        @NotNull
        @JsonProperty("username")
        public String username;

        @NotNull
        @JsonProperty("password")
        public String password;
    }

    @Valid
    @NotNull
    @JsonProperty("internal-storage")
    public InternalStorage internalStorage;

    /**
     * Represents ObjectStorage API Server configuration used for temporate storage setup.
     */
    @Getter
    public static class TemporateStorage {
        @NotNull
        @JsonProperty("frequency")
        public String frequency;
    }

    @Valid
    @NotNull
    @JsonProperty("temporate-storage")
    public TemporateStorage temporateStorage;

    /**
     * Represents ObjectStorage API Server configuration used for backup setup.
     */
    @Getter
    @NoArgsConstructor
    public static class Backup {
        @NotNull
        @JsonProperty("enabled")
        public Boolean enabled;

        @NotNull
        @JsonProperty("frequency")
        public String frequency;

        @NotNull
        @JsonProperty("max-versions")
        public Integer maxVersions;
    }

    @JsonProperty("backup")
    public Backup backup;

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
         * Represents ObjectStorage API Server configuration used for Grafana instance
         * setup.
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
         * Represents ObjectStorage API Server configuration used for Prometheus
         * instance setup.
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
         * Represents ObjectStorage API Server configuration used for Prometheus Node
         * Exporter instance setup.
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
}