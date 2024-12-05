package com.objectstorage.repository.executor;

import com.objectstorage.entity.common.ConfigEntity;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.repository.common.RepositoryConfigurationHelper;
import com.objectstorage.service.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service used to perform low-level database related operations.
 */
@ApplicationScoped
public class RepositoryExecutor {
    private static final Logger logger = LogManager.getLogger(RepositoryExecutor.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    DataSource dataSource;

    @Inject
    RepositoryConfigurationHelper repositoryConfigurationHelper;

    private Connection connection;

    private final List<Statement> statements = new ArrayList<>();

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Configures remote database connection.
     *
     * @throws QueryExecutionFailureException if connection retrieval fails.
     */
    @PostConstruct
    private void configure() throws QueryExecutionFailureException {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        if (configService.getConfig().getInternalStorage().getProvider() ==
                ConfigEntity.InternalStorage.Provider.POSTGRES) {

            try {
                performQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", properties.getDatabaseName()));
            } catch (QueryEmptyResultException e) {
                throw new QueryExecutionFailureException(e.getMessage());
            }
        }
    }

    /**
     * Performs given SQL query without result.
     *
     * @param query given SQL query to be executed.
     * @throws QueryExecutionFailureException if query execution is interrupted by failure.
     * @throws QueryEmptyResultException if result is empty.
     */
    public void performQuery(String query) throws QueryExecutionFailureException, QueryEmptyResultException {
        Statement statement;

        try {
            statement = this.connection.createStatement();
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        statements.add(statement);

        scheduledExecutorService.schedule(() -> {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.fatal(new QueryExecutionFailureException(e.getMessage()).getMessage());
            }
        }, properties.getDatabaseStatementCloseDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Performs given SQL query and returns raw result.
     *
     * @param query given SQL query to be executed.
     * @return retrieved raw result.
     * @throws QueryExecutionFailureException if query execution is interrupted by failure.
     * @throws QueryEmptyResultException if result is empty.
     */
    public ResultSet performQueryWithResult(String query) throws QueryExecutionFailureException, QueryEmptyResultException {
        Statement statement;

        try {
            statement = this.connection.createStatement();
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        ResultSet resultSet;

        try {
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        statements.add(statement);

        scheduledExecutorService.schedule(() -> {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.fatal(new QueryExecutionFailureException(e.getMessage()).getMessage());
            }
        }, properties.getDatabaseStatementCloseDelay(), TimeUnit.MILLISECONDS);

        try {
            if (!resultSet.isBeforeFirst()) {
                resultSet.close();

                throw new QueryEmptyResultException();
            }
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage());
        }

        return resultSet;
    }

    /**
     * Begins new transaction.
     *
     * @throws TransactionInitializationFailureException if transaction initialization fails.
     */
    public void beginTransaction() throws TransactionInitializationFailureException {
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionInitializationFailureException(e.getMessage());
        }
    }

    /**
     * Commits previously initialized transaction.
     *
     * @throws TransactionCommitFailureException if transaction commit fails.
     */
    public void commitTransaction() throws TransactionCommitFailureException {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            throw new TransactionCommitFailureException(e.getMessage());
        }

        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionCommitFailureException(e.getMessage());
        }
    }

    /**
     * Rollbacks previously initialized transaction.
     *
     * @throws TransactionRollbackFailureException if transaction rollback fails.
     */
    public void rollbackTransaction() throws TransactionRollbackFailureException {
        try {
            this.connection.rollback();
        } catch (SQLException e) {
            throw new TransactionRollbackFailureException(e.getMessage());
        }

        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionRollbackFailureException(e.getMessage());
        }
    }

    /**
     * Closes opened database connection.
     */
    @PreDestroy
    private void close() {
        statements.forEach(element -> {
            try {
                if (!element.isClosed()) {
                    element.close();
                }
            } catch (SQLException e) {
                logger.fatal(new QueryExecutionFailureException(e.getMessage()).getMessage());
            }
        });

        try {
            this.connection.close();
        } catch (SQLException e) {
            logger.fatal(e.getMessage());
        }
    }
}
