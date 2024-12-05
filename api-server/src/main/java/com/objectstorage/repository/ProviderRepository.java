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

        try {
            if (resultSet.next()) {
                try {
                    Integer id = resultSet.getInt("id");

                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        throw new RepositoryOperationFailureException(e.getMessage());
                    }

                    return ProviderEntity.of(id, name);
                } catch (SQLException e1) {
                    try {
                        resultSet.close();
                    } catch (SQLException e2) {
                        throw new RepositoryOperationFailureException(e2.getMessage());
                    }

                    throw new RepositoryOperationFailureException(e1.getMessage());
                }
            }
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

        return null;
    }

    /**
     * Attempts to retrieve provider entity by the given identificator.
     *
     * @param id given identificator of the configuration.
     * @return retrieved provider entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public ProviderEntity findById(Integer id) throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.name FROM %s as t WHERE t.id = '%s'",
                                    properties.getDatabaseProviderTableName(),
                                    id));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        try {
            if (resultSet.next()) {
                try {
                    String name = resultSet.getString("name");

                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        throw new RepositoryOperationFailureException(e.getMessage());
                    }

                    return ProviderEntity.of(id, name);
                } catch (SQLException e1) {
                    try {
                        resultSet.close();
                    } catch (SQLException e2) {
                        throw new RepositoryOperationFailureException(e2.getMessage());
                    }

                    throw new RepositoryOperationFailureException(e1.getMessage());
                }
            }
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

        return null;
    }
}
