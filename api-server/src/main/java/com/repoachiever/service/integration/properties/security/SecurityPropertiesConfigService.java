package com.objectstorage.service.integration.properties.security;

import com.repoachiever.exception.ConfigSecurityPropertiesMissingException;
import com.repoachiever.service.config.common.ConfigConfigurationHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.PropertiesConfigSource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;
import com.objectstorage.entity.common.ConfigEntity;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

///**
// * Service used to perform security properties configuration operations.
// */
//@StaticInitSafe
//public class SecurityPropertiesConfigService implements ConfigSource {
//    @ConfigProperty(name = "config.location")
//    String configLocation;
//
//    private final Properties config = new Properties();;
//
//    public SecurityPropertiesConfigService() {
//        System.out.println(configLocation);
////        ConfigConfigurationHelper.readConfig(configLocation)
////        config.put(
////                "quarkus.http.ssl.certificate.key-store-file",
////                configService.getConfig().getConnection().getSsl().getFile());
////        config.put(
////                "quarkus.http.ssl.certificate.key-store-password",
////                configService.getConfig().getConnection().getSsl().getPassword());
//    }
//
//    /**
//     * @see ConfigSource
//     */
//    @Override
//    public Map<String, String> getProperties() {
//        return ConfigSource.super.getProperties();
//    }
//
//    /**
//     * @see ConfigSource
//     */
//    @Override
//    public Set<String> getPropertyNames() {
//        return config.keySet().stream().map(element -> (String) element).collect(Collectors.toSet());
//    }
//
//    /**
//     * @see ConfigSource
//     */
//    @Override
//    public int getOrdinal() { return ConfigSource.super.getOrdinal(); }
//
//    /**
//     * @see ConfigSource
//     */
//    @Override
//    public String getValue(String key) {
//        return (String) config.get(key);
//    }
//
//    /**
//     * @see ConfigSource
//     */
//    @Override
//    public String getName() {
//        return SecurityPropertiesConfigService.class.getSimpleName();
//    }
//}


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

        if (Objects.isNull(config.getConnection().getSecurity().getFile()) ||
                Objects.isNull(config.getConnection().getSecurity().getPassword())) {
            throw new ConfigSecurityPropertiesMissingException();
        }

        properties.put(
                "quarkus.http.ssl.certificate.key-store-file",
                config.getConnection().getSecurity().getFile());
        properties.put(
                "quarkus.http.ssl.certificate.key-store-password",
                config.getConnection().getSecurity().getPassword());
        properties.put("quarkus.http.insecure-requests", "redirect");

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
