package com.aisa.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

/**
 * OracleDBUtil provides utility methods to interact with an Oracle database,
 * including methods for connecting to the database, executing queries, 
 * and mapping results to POJOs.
 */
public class OracleDBUtilLib {

    private static final Logger logger = LoggerFactory.getLogger(OracleDBUtilLib.class);

    // Load the Oracle JDBC driver
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            logger.info("Oracle JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            logger.error("Oracle JDBC driver not found.", e);
            throw new RuntimeException("Oracle JDBC driver not found", e);
        }
    }

    /**
     * Gets a connection to the Oracle database.
     *
     * @param url the JDBC URL of the Oracle database.
     * @param username the username to connect to the database.
     * @param password the password to connect to the database.
     * @return a Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection(String url, String username, String password) throws SQLException {
        logger.info("Getting connection to Oracle database.");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Gets a connection to the Oracle database using a Properties object.
     *
     * @param url the JDBC URL of the Oracle database.
     * @param properties the Properties object containing the connection properties.
     * @return a Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection(String url, Properties properties) throws SQLException {
        logger.info("Getting connection to Oracle database with properties.");
        return DriverManager.getConnection(url, properties);
    }

    /**
     * Executes a query on the Oracle database.
     *
     * @param connection the Connection object.
     * @param query the SQL query to execute.
     * @return a ResultSet object containing the result of the query.
     * @throws SQLException if a database access error occurs.
     */
    public static ResultSet executeQuery(Connection connection, String query) throws SQLException {
        logger.info("Executing query: {}", query);
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    /**
     * Executes an update on the Oracle database.
     *
     * @param connection the Connection object.
     * @param query the SQL query to execute.
     * @return the number of rows affected by the update.
     * @throws SQLException if a database access error occurs.
     */
    public static int executeUpdate(Connection connection, String query) throws SQLException {
        logger.info("Executing update: {}", query);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    /**
     * Closes the connection to the Oracle database.
     *
     * @param connection the Connection object to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Oracle database connection closed.");
            } catch (SQLException e) {
                logger.error("Error closing Oracle database connection.", e);
            }
        }
    }

    /**
     * Closes the ResultSet.
     *
     * @param resultSet the ResultSet object to close.
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
                logger.info("ResultSet closed.");
            } catch (SQLException e) {
                logger.error("Error closing ResultSet.", e);
            }
        }
    }

    /**
     * Closes the Statement.
     *
     * @param statement the Statement object to close.
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
                logger.info("Statement closed.");
            } catch (SQLException e) {
                logger.error("Error closing Statement.", e);
            }
        }
    }

    /**
     * Converts a given ResultSet into a nested list structure ( List of List of Maps<String,Object> ).
     *
     * The outer list contains rows, each of which is represented as a list of column-value mappings.
     * Each column-value mapping is represented as a Map where the key is the column name and the value
     * is the column value.
     *
     * @param resultSet the ResultSet to be converted
     * @return a List of rows, where each row is a List of column-value Maps
     * @throws SQLException if there is an error accessing the ResultSet
     */

    public static List<List<Map<String, Object>>> parseResultSet(ResultSet resultSet) throws SQLException {
        List<List<Map<String, Object>>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put(metaData.getColumnName(i), resultSet.getObject(i));
                rowList.add(columnMap);
            }
            resultList.add(rowList);
        }

        return resultList;
    }

    /**
     * Maps a ResultSet to a list of POJOs.
     *
     * @param resultSet the ResultSet object.
     * @param pojoClass the class of the POJO.
     * @param <T> the type of the POJO.
     * @return a list of POJOs.
     * @throws SQLException if a database access error occurs.
     */
    public static <T> List<T> mapResultSetToPOJO(ResultSet resultSet, Class<T> pojoClass) throws SQLException {
        logger.info("Mapping ResultSet to POJO: {}", pojoClass.getName());
        List<T> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        try {
            while (resultSet.next()) {
                T pojo = pojoClass.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    try {
                        pojoClass.getMethod("set" + capitalize(columnName), columnValue.getClass())
                                .invoke(pojo, columnValue);
                    } catch (NoSuchMethodException e) {
                        logger.warn("No setter for {} in {}", columnName, pojoClass.getName());
                    }
                }
                resultList.add(pojo);
            }
        } catch (Exception e) {
            logger.error("Failed to map ResultSet to POJO.", e);
            throw new SQLException("Failed to map ResultSet to POJO", e);
        }

        logger.info("ResultSet mapped to POJO: {}", pojoClass.getName());
        return resultList;
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize.
     * @return the capitalized string.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Example POJO for demonstration purposes.
     */
    public static class ExamplePOJO {
        private int id;
        private String name;

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Example usage of the OracleDBUtil.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String username = "your_username";
        String password = "your_password";
        String query = "SELECT * FROM your_table";

        try {
            Connection connection = getConnection(url, username, password);
            ResultSet resultSet = executeQuery(connection, query);

            List<ExamplePOJO> resultList = mapResultSetToPOJO(resultSet, ExamplePOJO.class);

            // Process the result list
            for (ExamplePOJO pojo : resultList) {
                System.out.println("ID: " + pojo.getId() + ", Name: " + pojo.getName());
            }

            // Close resources
            closeResultSet(resultSet);
            closeConnection(connection);
        } catch (SQLException e) {
            logger.error("Database operation failed.", e);
        }
    }
}