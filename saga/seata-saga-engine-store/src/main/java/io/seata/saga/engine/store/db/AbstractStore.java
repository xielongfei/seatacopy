package io.seata.saga.engine.store.db;

import io.seata.common.exception.StoreException;
import io.seata.common.util.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: xielongfei
 * @date: 2021/09/10
 * @description:
 */
public abstract class AbstractStore {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStore.class);

    protected DataSource dataSource;

    protected String dbType;

    protected String tablePrefix;

    public static void closeSilent(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.info(e.getMessage(), e);
            }
        }
    }

    protected <T> T selectOne(String sql, ResultSetToObject<T> resultSetToObject, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Preparing SQL statement: {}", sql);
            }

            stmt = connection.prepareStatement(sql);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("setting params to PreparedStatement: {}", Arrays.toString(args));
            }

            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSetToObject.toObject(resultSet);
            }
        } catch (SQLException e) {
            throw new StoreException(e);
        } finally {
            closeSilent(resultSet);
            closeSilent(stmt);
            closeSilent(connection);
        }
        return null;
    }

    protected <T> List<T> selectList(String sql, ResultSetToObject<T> resultSetToObject, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Preparing SQL: {}", sql);
            }

            stmt = connection.prepareStatement(sql);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("setting params to PreparedStatement: {}", Arrays.toString(args));
            }

            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            resultSet = stmt.executeQuery();
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSetToObject.toObject(resultSet));
            }
            return list;
        } catch (SQLException e) {
            throw new StoreException(e);
        } finally {
            closeSilent(resultSet);
            closeSilent(stmt);
            closeSilent(connection);
        }
    }

    protected <T> int executeUpdate(String sql, ObjectToStatement<T> objectToStatement, T o) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = dataSource.getConnection();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Preparing SQL: {}", sql);
            }

            stmt = connection.prepareStatement(sql);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("setting params to PreparedStatement: {}", BeanUtils.beanToString(o));
            }

            objectToStatement.toStatement(o, stmt);
            int count = stmt.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return count;
        } catch (SQLException e) {
            throw new StoreException(e);
        } finally {
            closeSilent(stmt);
            closeSilent(connection);
        }
    }

    protected int executeUpdate(String sql, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = dataSource.getConnection();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Preparing SQL: {}", sql);
            }

            stmt = connection.prepareStatement(sql);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("setting params to PreparedStatement: {}", Arrays.toString(args));
            }

            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            int count = stmt.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return count;
        } catch (SQLException e) {
            throw new StoreException(e);
        } finally {
            closeSilent(stmt);
            closeSilent(connection);
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    protected interface ResultSetToObject<T> {

        T toObject(ResultSet resultSet) throws SQLException;
    }

    protected interface ObjectToStatement<T> {

        void toStatement(T o, PreparedStatement statement) throws SQLException;
    }
}
