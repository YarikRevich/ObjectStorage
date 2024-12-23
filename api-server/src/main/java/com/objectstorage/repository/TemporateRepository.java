package com.objectstorage.repository;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.entity.repository.ContentEntity;
import com.objectstorage.entity.repository.TemporateEntity;
import com.objectstorage.exception.QueryEmptyResultException;
import com.objectstorage.exception.QueryExecutionFailureException;
import com.objectstorage.exception.RepositoryOperationFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import com.objectstorage.service.config.ConfigService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
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
    ConfigService configService;

    @Inject
    RepositoryExecutor repositoryExecutor;

    /**
     * Inserts given values into the temporate table.
     *
     * @param provider given provider.
     * @param secret   given secret.
     * @param location given file location.
     * @param hash given file name hash.
     * @param createdAt given creation timestamp.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void insert(Integer provider, Integer secret, String location, String hash, Long createdAt)
            throws RepositoryOperationFailureException {
        String query = String.format(
                "INSERT INTO %s (provider, secret, location, hash, created_at) VALUES (%d, %d, '%s', '%s', %d)",
                properties.getDatabaseTemporateTableName(),
                provider,
                secret,
                location,
                hash,
                createdAt);

        try {
            repositoryExecutor.performQuery(query);

        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves amount of temporate content entities.
     *
     * @return retrieved amount of temporate content entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public Integer count() throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format("SELECT COUNT(1) as result FROM %s", properties.getDatabaseTemporateTableName()));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        Integer count = 0;

        try {
            if (resultSet.next()) {
                switch (configService.getConfig().getInternalStorage().getProvider()) {
                    case SQLITE3 -> {
                        try {
                            count = resultSet.getInt("result");
                        } catch (SQLException e1) {
                            try {
                                resultSet.close();
                            } catch (SQLException e2) {
                                throw new RepositoryOperationFailureException(e2.getMessage());
                            }

                            throw new RepositoryOperationFailureException(e1.getMessage());
                        }
                    }
                    case POSTGRES -> {
                        try {
                            count = (int) resultSet.getLong("result");
                        } catch (SQLException e1) {
                            try {
                                resultSet.close();
                            } catch (SQLException e2) {
                                throw new RepositoryOperationFailureException(e2.getMessage());
                            }

                            throw new RepositoryOperationFailureException(e1.getMessage());
                        }
                    }
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

        return count;
    }

    /**
     * Retrieves the earliest temporate content entity.
     *
     * @return retrieved temporate entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public TemporateEntity findEarliest() throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.provider, t.secret, t.location, t.hash, t.created_at FROM %s as t ORDER BY t.created_at DESC LIMIT 1",
                                    properties.getDatabaseTemporateTableName()));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        try {
            if (resultSet.next()) {
                try {
                    Integer id = resultSet.getInt("id");
                    Integer provider = resultSet.getInt("provider");
                    Integer secret = resultSet.getInt("secret");
                    String location = resultSet.getString("location");
                    String hash = resultSet.getString("hash");
                    Long createdAt = resultSet.getLong("created_at");

                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        throw new RepositoryOperationFailureException(e.getMessage());
                    }

                    return TemporateEntity.of(id, provider, secret, location, hash, createdAt);
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
     * Retrieves all the persisted temporate entities with the given hash.
     *
     * @param hash given hash.
     * @return retrieved temporate entities.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public List<TemporateEntity> findByHash(String hash) throws
            RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.location, t.provider, t.secret, t.created_at FROM %s as t WHERE t.hash = '%s'",
                                    properties.getDatabaseTemporateTableName(),
                                    hash));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<TemporateEntity> result = new ArrayList<>();

        Integer id;
        String location;
        Integer provider;
        Integer secret;
        Long createdAt;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                location = resultSet.getString("location");
                provider = resultSet.getInt("provider");
                secret = resultSet.getInt("secret");
                createdAt = resultSet.getLong("created_at");

                result.add(TemporateEntity.of(id, provider, secret, location, hash, createdAt));
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
     * Retrieves all the persisted temporate entities with the given location, provider and secret.
     *
     * @param location given location.
     * @param provider given provider.
     * @param secret given secret.
     * @return retrieved temporate entity.
     * @throws RepositoryOperationFailureException if repository operation fails.
     */
    public TemporateEntity findEarliestByLocationProviderAndSecret(
            String location, Integer provider, Integer secret) throws RepositoryOperationFailureException {
        ResultSet resultSet;

        try {
            resultSet =
                    repositoryExecutor.performQueryWithResult(
                            String.format(
                                    "SELECT t.id, t.hash, t.created_at FROM %s as t WHERE t.location = '%s' AND t.provider = %d AND t.secret = %d ORDER BY created_at DESC LIMIT 1",
                                    properties.getDatabaseTemporateTableName(),
                                    location,
                                    provider,
                                    secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        try {
            if (resultSet.next()) {
                try {
                    Integer id = resultSet.getInt("id");
                    String hash = resultSet.getString("hash");
                    Long createdAt = resultSet.getLong("created_at");

                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        throw new RepositoryOperationFailureException(e.getMessage());
                    }

                    return TemporateEntity.of(id, provider, secret, location, hash, createdAt);
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
     * Retrieves all the persisted temporate entities with the given provider and secret.
     *
     * @param provider given provider.
     * @param secret given secret.
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
                                    "SELECT t.id, t.location, t.hash, t.created_at FROM %s as t WHERE t.provider = %d AND t.secret = %d",
                                    properties.getDatabaseTemporateTableName(),
                                    provider,
                                    secret));

        } catch (QueryExecutionFailureException | QueryEmptyResultException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }

        List<TemporateEntity> result = new ArrayList<>();

        Integer id;
        String location;
        String hash;
        Long createdAt;

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                location = resultSet.getString("location");
                hash = resultSet.getString("hash");
                createdAt = resultSet.getLong("created_at");

                result.add(TemporateEntity.of(id, provider, secret, location, hash, createdAt));
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
     * Deletes entity with the given location, provider and secret from temporate table.
     *
     * @param location given location.
     * @param provider given provider.
     * @param secret given secret.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void deleteByLocationProviderAndSecret(String location, Integer provider, Integer secret)
            throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "DELETE FROM %s as t WHERE t.location = '%s' AND t.provider = %d AND t.secret = %d",
                            properties.getDatabaseTemporateTableName(),
                            location,
                            provider,
                            secret));

        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }

    /**
     * Deletes entity with the given hash from temporate table.
     *
     * @param hash given hash.
     * @throws RepositoryOperationFailureException if operation execution fails.
     */
    public void deleteByHash(String hash) throws RepositoryOperationFailureException {
        try {
            repositoryExecutor.performQuery(
                    String.format(
                            "DELETE FROM %s as t WHERE t.hash = '%s'",
                            properties.getDatabaseTemporateTableName(),
                            hash));

        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
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

        } catch (QueryExecutionFailureException e) {
            throw new RepositoryOperationFailureException(e.getMessage());
        }
    }
}
