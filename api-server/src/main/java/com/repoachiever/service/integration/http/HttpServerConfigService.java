package com.objectstorage.service.integration.http;

import com.objectstorage.entity.common.ConfigEntity;
import com.objectstorage.service.config.ConfigService;
import io.quarkus.runtime.Startup;
import io.quarkus.tls.BaseTlsConfiguration;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

/**
 * Provides http server configuration used as a source of properties for all defined resources.
 */
@Startup(value = 300)
@ApplicationScoped
public class HttpServerConfigService implements HttpServerOptionsCustomizer {
    @Inject
    ConfigService configService;

    /**
     * @see HttpServerOptionsCustomizer
     */
    @SneakyThrows
    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        options.setPort(configService.getConfig().getConnection().getPort());
    }
}

