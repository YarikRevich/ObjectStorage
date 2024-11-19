package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ConfigEntity;
import com.objectstorage.exception.QueryEmptyResultException;
import com.objectstorage.exception.QueryExecutionFailureException;
import com.objectstorage.exception.RepositoryOperationFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents repository implementation to handle config table.
 */
@ApplicationScoped
@RegisterForReflection
public class ConfigRepository {
    @Inject
    PropertiesEntity properties;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the config table.
     *
     * @param provider given provider.
     * @param secret   given secret.
     * @param hash given configuration file hash.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(Integer provider, Integer secret, String hash) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                            String.format(
                                    "INSERT INTO %s (provider, secret, hash) VALUES (%d, %d, '%s')",
                                    properties.getDatabaseConfigTableName(),
                                    provider,
                                    secret,
                                    hash));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves all the persisted temporate entities with the given provider and secret.
     *
     * @return retrieved temporate entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public List<ConfigEntity> findByProviderAndSecret(Integer provider, Integer secret) throws
            RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.hash FROM %s as t WHERE t.provider = %d AND t.secret = %d",
                                    properties.getDatabaseConfigTableName(),
                                    provider,
                                    secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<ConfigEntity> result = new ArrayList<>();

        Integer id;
        String hash;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                hash = resultSet.getString("hash");

                result.add(ConfigEntity.of(id, provider, secret, hash));
            }
        } catch (SQLException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        return result;
    }

    /**
     * Deletes all entities with the given provider and secret from config table.
     *
     * @param provider given provider.
     * @param secret given secret.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void deleteByProviderAndSecret(Integer provider, Integer secret) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "DELETE FROM %s as t WHERE t.provider = %d AND t.secret = %d",
                            properties.getDatabaseConfigTableName(),
                            provider,
                            secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}
