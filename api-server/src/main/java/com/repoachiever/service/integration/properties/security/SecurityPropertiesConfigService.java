package com.objectstorage.service.integration.properties.security;

import com.repoachiever.exception.ConfigSecurityPropertiesMissingException;
import com.repoachiever.service.config.common.ConfigConfigurationHelper;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.PropertiesConfigSource;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.spi.ConfigSource;
import com.objectstorage.entity.common.ConfigEntity;

import java.util.Collections;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Properties;

/**
 * Service used to perform security properties configuration operations.
 */
@StaticInitSafe
public class SecurityPropertiesConfigService implements ConfigSourceFactory {
    @Override
    @SneakyThrows
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context) {
        final ConfigValue value = context.getValue("config.location");
        if (value == null || value.getValue() == null) {
            return Collections.emptyList();
        }

        Properties properties = new Properties();

        ConfigEntity config = ConfigConfigurationHelper.readConfig(value.getValue(), false);
        if (Objects.isNull(config)) {
            return Collections.emptyList();
        }

        if (Objects.isNull(config.getConnection()) ||
                Objects.isNull(config.getConnection().getSecurity()) ||
                !config.getConnection().getSecurity().getEnabled()) {
            return Collections.emptyList();
        }

        if (Objects.isNull(config.getConnection().getPort()) ||
                Objects.isNull(config.getConnection().getSecurity().getFile()) ||
                Objects.isNull(config.getConnection().getSecurity().getPassword())) {
            throw new ConfigSecurityPropertiesMissingException();
        }

        properties.put(
                "quarkus.http.ssl.certificate.key-store-file",
                config.getConnection().getSecurity().getFile());
        properties.put(
                "quarkus.http.ssl.certificate.key-store-password",
                config.getConnection().getSecurity().getPassword());
        properties.put("quarkus.http.insecure-requests", "disabled");
        properties.put("quarkus.http.ssl-port", config.getConnection().getPort());

        return Collections.singletonList(
                new PropertiesConfigSource(
                        properties,
                        SecurityPropertiesConfigService.class.getSimpleName(),
                        290));
    }

    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(290);
    }
}