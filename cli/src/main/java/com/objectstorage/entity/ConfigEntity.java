package com.objectstorage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Service used to perform ObjectStorage CLI processing operation.
 */
@Getter
public class ConfigEntity {
    /**
     * Represents ObjectStorage CLI configuration used for content management.
     */
    @Getter
    public static class Content {
        @NotNull
        @JsonProperty("root")
        public String root;
    }

    @Valid
    @NotNull
    @JsonProperty("content")
    public Content content;

    /**
     * Represents service configurations for ObjectStorage API Server used for operations processing.
     */
    @Getter
    public static class Service {
        /**
         * Represents all supported service providers, which can be used by ObjectStorage API Server.
         */
        public enum Provider {
            @JsonProperty("s3")
            S3("s3"),

            @JsonProperty("gcs")
            GCS("gcs");

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

        /**
         * Represents credentials used for selected provider authentication.
         */
        @Getter
        @NoArgsConstructor
        public static class Credentials {
            @NotNull
            public Integer id;

            @NotNull
            @Pattern(regexp = "^(~|\\.|\\/)?([a-zA-Z0-9_\\-/\\.]+)?$")
            public String file;

            @Nullable
            public String region;
        }

        @Valid
        @NotNull
        @JsonProperty("credentials")
        public Credentials credentials;
    }

    @Valid
    @NotNull
    @JsonProperty("service")
    public List<Service> service;

    /**
     * Represents ObjectStorage API Server configuration used for further connection establishment.
     */
    @Getter
    public static class APIServer {
        @NotBlank
        public String host;
    }

    @Valid
    @NotNull
    @JsonProperty("api-server")
    public APIServer apiServer;
}
