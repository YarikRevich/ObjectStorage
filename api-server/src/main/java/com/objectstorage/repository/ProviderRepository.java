package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ConfigEntity;
import com.objectstorage.entity.repository.ProviderEntity;
import com.objectstorage.exception.QueryEmptyResultException;
import com.objectstorage.exception.QueryExecutionFailureException;
import com.objectstorage.exception.RepositoryOperationFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents repository implementation to handle provider table.
 */
@ApplicationScoped
@RegisterForReflection
public class ProviderRepository {
    @Inject
    PropertiesEntity properties;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the provider table.
     *
     * @param name given provider name.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(String name) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "INSERT INTO %s (name) VALUES ('%s')",
                            properties.getDatabaseProviderTableName(),
                            name));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Checks if provider entity with the given name is present.
     *
     * @param name given name of the provider.
     * @return result of the check.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public Boolean isPresentByName(String name) throws RepositoryOperationFailureException {
        try {
            ResultSet resultSet = repositoryExecutor.performQueryWithResult(
                    String.format(
                            "SELECT t.id FROM %s as t WHERE t.name = '%s'",
                            properties.getDatabaseProviderTableName(),
                            name));

            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RepositoryOperationFailureException(e.getMessage());
            }
        } catch (QueryEmptyResultException e) {
            return false;
        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        return true;
    }

    /**
     * Attempts to retrieve provider entity by the given name.
     *
     * @param name given name of the provider.
     * @return retrieved config entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public ProviderEntity findByName(String name) throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id FROM %s as t WHERE t.name = '%s'",
                                    properties.getDatabaseProviderTableName(),
                                    name));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        Integer id;

        try {
            id = resultSet.getInt("id");
        } catch (SQLException e1) {
            try {
                resultSet.close();
            } catch (SQLException e2) {
                throw new RepositoryOperationFailureException(e2.getMessage());
            }

            throw new RepositoryOperationFailureException(e1.getMessage());
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        return ProviderEntity.of(id, name);
    }
}
