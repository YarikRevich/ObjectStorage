package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ContentEntity;
import com.objectstorage.entity.repository.TemporateEntity;
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
 * Represents repository implementation to handle temporate table.
 */
@ApplicationScoped
@RegisterForReflection
public class TemporateRepository {
    @Inject
    PropertiesEntity properties;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the temporate table.
     *
     * @param provider given provider.
     * @param secret   given secret.
     * @param hash given file name hash.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(Integer provider, Integer secret, String hash) throws RepositoryOperationFailureException {
        String query = String.format(
                "INSERT INTO %s (provider, secret, hash) VALUES (%d, %d, '%s')",
                properties.getDatabaseTemporateTableName(),
                provider,
                secret,
                hash);

        try {
            repositoryExecutor.performQuery(query);

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
    public List<TemporateEntity> findByProviderAndSecret(Integer provider, Integer secret) throws
            RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.hash FROM %s as t WHERE t.provider = %d AND t.secret = %d",
                                    properties.getDatabaseTemporateTableName(),
                                    provider,
                                    secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<TemporateEntity> result = new ArrayList<>();

        Integer id;
        String hash;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                hash = resultSet.getString("hash");

                result.add(TemporateEntity.of(id, provider, secret, hash));
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
     * Retrieves all the persisted temporate entities.
     *
     * @return retrieved temporate entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public List<TemporateEntity> findAll() throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.provider, t.secret, t.hash FROM %s as t",
                                    properties.getDatabaseTemporateTableName()));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<TemporateEntity> result = new ArrayList<>();

        Integer id;
        Integer provider;
        Integer secret;
        String hash;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                provider = resultSet.getInt("provider");
                secret = resultSet.getInt("secret");
                hash = resultSet.getString("hash");

                result.add(TemporateEntity.of(id, provider, secret, hash));
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
     * Deletes all entities with the given provider and secret from temporate table.
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
                            properties.getDatabaseTemporateTableName(),
                            provider,
                            secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}