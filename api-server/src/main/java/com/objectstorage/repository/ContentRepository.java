package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ContentEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents repository implementation to handle content table.
 */
@ApplicationScoped
@RegisterForReflection
public class ContentRepository {
    @Inject
    PropertiesEntity properties;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the content table.
     *
     * @param root given file root location.
     * @param provider given provider used for content retrieval.
     * @param secret   given secret, which allows content retrieval.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(String root, Integer provider, Integer secret) throws RepositoryOperationFailureException {
        String query = String.format(
                "INSERT INTO %s (root, provider, secret) VALUES ('%s', %d, %d)",
                properties.getDatabaseContentTableName(),
                root,
                provider,
                secret);

        try {
            repositoryExecutor.performQuery(query);

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves all the persisted content entities with the given provider and secret.
     *
     * @return retrieved content entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public List<ContentEntity> findByProviderAndSecret(Integer provider, Integer secret) throws
            RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.root FROM %s as t WHERE t.provider = %d AND t.secret = %d",
                                    properties.getDatabaseContentTableName(),
                                    provider,
                                    secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<ContentEntity> result = new ArrayList<>();

        Integer id;
        String root;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                root = resultSet.getString("root");

                result.add(ContentEntity.of(id, root, provider, secret));
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
     * Retrieves all the persisted content entities.
     *
     * @return retrieved content entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public List<ContentEntity> findAll() throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.root, t.provider, t.secret FROM %s as t",
                                    properties.getDatabaseContentTableName()));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<ContentEntity> result = new ArrayList<>();

        Integer id;
        String root;
        Integer provider;
        Integer secret;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                root = resultSet.getString("root");
                provider = resultSet.getInt("provider");
                secret = resultSet.getInt("secret");

                result.add(ContentEntity.of(id, root, provider, secret));
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
     * Deletes all entities with the given secret from content table.
     *
     * @param secret given secret, which allows content retrieval.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void deleteBySecret(Integer secret) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "DELETE FROM %s as t WHERE t.secret = %d",
                            properties.getDatabaseContentTableName(),
                            secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}
