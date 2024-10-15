package com.objectstorage.service.integration.http;

import com.objectstorage.entity.common.ConfigEntity;
import com.objectstorage.service.config.ConfigService;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides http server configuration used as a source of properties for all defined resources.
 */
@ApplicationScoped
public class HttpServerConfigService implements HttpServerOptionsCustomizer {
    @Inject
    ConfigService configService;

    /**
     * @see HttpServerOptionsCustomizer
     */
    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        ConfigEntity.Connection connection = configService.getConfig().getConnection();

        options.setPort(connection.getPort());
    }
}

