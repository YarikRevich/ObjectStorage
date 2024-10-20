package com.objectstorage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import lombok.Getter;

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
        public Stirng root;
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
            S3("s3");

            private final String value;

            Provider(String value) {
                this.value = value;
            }

            public String toString() {
                return value;
            }
        }

        @JsonProperty("provider")
        public Provider provider;

        /**
         * Represents section used for ObjectStorage API Server backup configuration.
         */
        @Getter
        @NoArgsConstructor
        public static class Backup {
            @NotNull
            @JsonProperty("enabled")
            public Boolean enabled;

            /**
             * Represents all supported content formats, which can be used by ObjectStorage backup service.
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

            @NotNull
            @JsonProperty("frequency")
            public String frequency;
        }

        @JsonProperty("backup")
        public Backup backup;

        /**
         * Represents credentials used for service provider authentication.
         */
        @Getter
        @NoArgsConstructor
        public static class AWSCredentials {
            @Pattern(regexp = "^(((./)?)|((~/.)?)|((/?))?)([a-zA-Z/]*)((\\.([a-z]+))?)$")
            public String file;

            @NotBlank
            public String region;
        }

        @NotNull
        public Object credentials;
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
