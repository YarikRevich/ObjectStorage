package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ProviderEntity;
import com.objectstorage.entity.repository.SecretEntity;
import com.objectstorage.exception.QueryEmptyResultException;
import com.objectstorage.exception.QueryExecutionFailureException;
import com.objectstorage.exception.RepositoryOperationFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/**
 * Represents repository implementation to handle secret table.
 */
@ApplicationScoped
@RegisterForReflection
public class SecretRepository {
    @Inject
    PropertiesEntity properties;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the provider table.
     *
     * @param session given internal secret.
     * @param credentials given signature of external credentials.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(Integer session, String credentials) throws RepositoryOperationFailureException {
        String query = String.format(
                "INSERT INTO %s (session, credentials) VALUES (%d, '%s')",
                properties.getDatabaseSecretTableName(),
                session,
                credentials);

        try {
            repositoryExecutor.performQuery(query);
        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Checks if secret entity with the given session and credentials is present.
     *
     * @param session given session of the secrets set.
     * @param credentials given signature of external credentials.
     * @return result of the check.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public Boolean isPresentBySessionAndCredentials(Integer session, String credentials) throws RepositoryOperationFailureException {
        String query = String.format(
                "SELECT t.id FROM %s as t WHERE t.session = %d AND t.credentials = '%s'",
                properties.getDatabaseSecretTableName(),
                session,
                credentials);

        ResultSet resultSet;

        try {
            resultSet = repositoryExecutor.performQueryWithResult(query);
        } catch (QueryEmptyResultException e) {
            return false;
        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        return true;
    }

    /**
     * Attempts to retrieve secret entity by the given session and credentials.
     *
     * @param session given session of the secrets set.
     * @param credentials given signature of external credentials.
     * @return retrieved secret entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public SecretEntity findBySessionAndCredentials(Integer session, String credentials) throws RepositoryOperationFailureException {
        String query = String.format(
                "SELECT t.id FROM %s as t WHERE t.session = %d AND t.credentials = '%s'",
                properties.getDatabaseSecretTableName(),
                session,
                credentials);

        ResultSet resultSet;

        try {
            resultSet = repositoryExecutor.performQueryWithResult(query);
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

        return SecretEntity.of(id, session, credentials);
    }

    /**
     * Attempts to retrieve secret entity by the given identificator.
     *
     * @param id given identificator of the secrets set.
     * @return retrieved secret entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public SecretEntity findById(Integer id) throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet = repositoryExecutor.performQueryWithResult(String.format(
                    "SELECT t.session, t.credentials FROM %s as t WHERE t.id = %d",
                    properties.getDatabaseSecretTableName(),
                    id));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        Integer session;

        try {
            session = resultSet.getInt("session");
        } catch (SQLException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        String credentials;

        try {
            credentials = resultSet.getString("credentials");
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

        return SecretEntity.of(id, session, credentials);
    }

    /**
     * Deletes entity with the given identificator from secret table.
     *
     * @param id given identificator of the secrets set.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void deleteById(Integer id) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "DELETE FROM %s as t WHERE t.id = %d",
                            properties.getDatabaseSecretTableName(),
                            id));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}
