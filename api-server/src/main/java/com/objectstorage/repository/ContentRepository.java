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
     * @param provider given provider.
     * @param secret   given secret.
     * @param root     given file root location.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(Integer provider, Integer secret, String root) throws RepositoryOperationFailureException {
        String query = String.format(
                "INSERT INTO %s (provider, secret, root) VALUES (%d, %d, '%s')",
                properties.getDatabaseContentTableName(),
                provider,
                secret,
                root);

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

                result.add(ContentEntity.of(id, provider, secret, root));
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

        return result;
    }

    /**
     * Deletes all entities with the given provider and secret from content table.
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
                            properties.getDatabaseContentTableName(),
                            provider,
                            secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}
