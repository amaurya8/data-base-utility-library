package com.aisa.database;

import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing operations on a Microsoft SQL Server database.
 *
 * This class provides various methods to interact with a MSSQL database, including
 * establishing connections, executing queries and updates,
 * and converting ResultSets to more usable formats.
 */
public class MSSQLUtilLib {

    private static final Logger logger = LoggerFactory.getLogger(MSSQLUtilLib.class);

    private static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    /**
     * Establishes a connection to the MSSQL database.
     *
     * @param dbUrl    the database URL in the format jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
     * @param user     the database user
     * @param password the user's password
     * @return the connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection(String dbUrl, String user, String password) throws SQLException {
        logger.info("Establishing connection to the MSSQL database.");
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("MSSQL JDBC driver not found.", e);
            throw new SQLException("MSSQL JDBC driver not found.", e);
        }

        // Append Azure SQL Database specific properties to the URL if not already present
        if (!dbUrl.contains("encrypt=")) {
            dbUrl += ";encrypt=true";
        }
        if (!dbUrl.contains("trustServerCertificate=")) {
            dbUrl += ";trustServerCertificate=false";
        }

        // For Azure SQL Database, you may also want to set additional properties like login timeout, etc.
        dbUrl += ";loginTimeout=30";

        return DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Executes a query on the MSSQL database.
     *
     * @param connection the connection object
     * @param query      the SQL query to be executed
     * @return the ResultSet object containing the results of the query
     * @throws SQLException if a database access error occurs
     */
    public static ResultSet executeQuery(Connection connection, String query) throws SQLException {
        logger.info("Executing query: {}", query);
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Executes an update on the MSSQL database.
     *
     * @param connection the connection object
     * @param update     the SQL update to be executed
     * @return the number of rows affected
     * @throws SQLException if a database access error occurs
     */
    public static int executeUpdate(Connection connection, String update) throws SQLException {
        logger.info("Executing update: {}", update);
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(update);
    }

    /**
     * Converts a ResultSet to a List of Maps.
     *
     * @param resultSet the ResultSet to be converted
     * @return a List of Maps, where each Map represents a row with column names as keys
     *         and column values as values
     * @throws SQLException if there is an error accessing the ResultSet
     */
    public static List<Map<String, Object>> resultSetToList(ResultSet resultSet) throws SQLException {
        logger.info("Converting ResultSet to List of Maps.");
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    /**
     * Commits the transaction on the given connection.
     *
     * @param connection the connection object
     * @throws SQLException if a database access error occurs
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        logger.info("Committing transaction.");
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rolls back the transaction on the given connection.
     *
     * @param connection the connection object
     * @throws SQLException if a database access error occurs
     */
    public static void rollbackTransaction(Connection connection) throws SQLException {
        logger.info("Rolling back transaction.");
        connection.rollback();
        connection.setAutoCommit(true);
    }

    /**
     * Closes the database connection.
     *
     * @param connection the connection object to be closed
     */
    public static void closeConnection(Connection connection) {
        logger.info("Closing database connection.");
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection.", e);
            }
        }
    }

    /**
     * Closes the database statement.
     *
     * @param statement the statement object to be closed
     */
    public static void closeStatement(Statement statement) {
        logger.info("Closing database statement.");
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("Error closing database statement.", e);
            }
        }
    }

    /**
     * Closes the database ResultSet.
     *
     * @param resultSet the ResultSet object to be closed
     */
    public static void closeResultSet(ResultSet resultSet) {
        logger.info("Closing database ResultSet.");
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Error closing database ResultSet.", e);
            }
        }
    }
}