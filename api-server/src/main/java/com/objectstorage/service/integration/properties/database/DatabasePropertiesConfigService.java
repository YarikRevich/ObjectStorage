package com.objectstorage.service.integration.properties.database;

import com.objectstorage.exception.ConfigDatabasePropertiesMissingException;
import com.objectstorage.exception.QueryExecutionFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import com.objectstorage.service.config.common.ConfigConfigurationHelper;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.PropertiesConfigSource;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.spi.ConfigSource;
import com.objectstorage.entity.common.ConfigEntity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Properties;

/**
 * Service used to perform security properties configuration operations.
 */
@StaticInitSafe
public class DatabasePropertiesConfigService implements ConfigSourceFactory {
    @Override
    @SneakyThrows
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context) {
        final ConfigValue configLocation = context.getValue("config.location");

        if (Objects.isNull(configLocation) || Objects.isNull(configLocation.getValue())) {
            return Collections.emptyList();
        }

        final ConfigValue databaseName = context.getValue("database.name");
        if (Objects.isNull(databaseName) || Objects.isNull(databaseName.getValue())) {
            return Collections.emptyList();
        }

        final ConfigValue liquibaseSqlite3Config = context.getValue("liquibase.sqlite3.config");
        if (Objects.isNull(liquibaseSqlite3Config) || Objects.isNull(liquibaseSqlite3Config.getValue())) {
            return Collections.emptyList();
        }

        final ConfigValue liquibasePostgresConfig = context.getValue("liquibase.postgres.config");
        if (Objects.isNull(liquibasePostgresConfig) || Objects.isNull(liquibasePostgresConfig.getValue())) {
            return Collections.emptyList();
        }

        final ConfigValue databaseStatementCloseDelay = context.getValue("database.statement.close-delay");
        if (Objects.isNull(databaseStatementCloseDelay) || Objects.isNull(databaseStatementCloseDelay.getValue())) {
            return Collections.emptyList();
        }

        Properties properties = new Properties();

        ConfigEntity config = ConfigConfigurationHelper.readConfig(configLocation.getValue(), false);
        if (Objects.isNull(config)) {
            return Collections.emptyList();
        }

        if (Objects.isNull(config.getInternalStorage()) ||
                Objects.isNull(config.getInternalStorage().getProvider()) ||
                Objects.isNull(config.getInternalStorage().getUsername()) ||
                Objects.isNull(config.getInternalStorage().getPassword())) {
            throw new ConfigDatabasePropertiesMissingException();
        }

        if (config.getInternalStorage().getProvider() == ConfigEntity.InternalStorage.Provider.POSTGRES &&
                Objects.isNull(config.getInternalStorage().getHost())) {
            throw new ConfigDatabasePropertiesMissingException();
        }

        switch (config.getInternalStorage().getProvider()) {
            case SQLITE3 -> {
                properties.put("quarkus.liquibase.change-log", liquibaseSqlite3Config.getValue());

                properties.put("quarkus.datasource.jdbc.driver", "org.sqlite.JDBC");
                properties.put("quarkus.datasource.db-kind", "other");
                properties.put(
                        "quarkus.datasource.jdbc.url",
                        String.format(
                                "jdbc:sqlite:%s/.%s/internal/database/data.db",
                                System.getProperty("user.home"),
                                databaseName.getValue()));
            }
            case POSTGRES -> {
                properties.put("quarkus.liquibase.change-log", liquibasePostgresConfig.getValue());

                properties.put("quarkus.datasource.db-kind", "postgresql");
                properties.put(
                        "quarkus.datasource.jdbc.url",
                        String.format("jdbc:postgresql://%s/%s",
                                config.getInternalStorage().getHost(),
                                databaseName.getValue()));

                Connection connection = DriverManager.getConnection(
                        String.format("jdbc:postgresql://%s/postgres", config.getInternalStorage().getHost()),
                        config.getInternalStorage().getUsername(),
                        config.getInternalStorage().getPassword());

                try {
                    RepositoryExecutor.performQuery(
                            connection,
                            String.format("CREATE DATABASE %s", databaseName.getValue()),
                            Integer.valueOf(databaseStatementCloseDelay.getValue()));
                } catch (QueryExecutionFailureException ignore) {
                }

                connection.close();
            }
        }

        properties.put("quarkus.datasource.username", config.getInternalStorage().getUsername());
        properties.put("quarkus.datasource.password", config.getInternalStorage().getPassword());

        return Collections.singletonList(
                new PropertiesConfigSource(
                        properties,
                        com.objectstorage.service.integration.properties.security.SecurityPropertiesConfigService.class.getSimpleName(),
                        290));
    }

    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(290);
    }
}