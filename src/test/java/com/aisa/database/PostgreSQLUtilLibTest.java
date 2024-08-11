package com.aisa.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostgreSQLUtilLibTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeEach
    public void setUp() throws SQLException {
        System.out.println("Opening Mocks..!");
        MockitoAnnotations.openMocks(this);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate(anyString())).thenReturn(1);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(1);
        when(mockResultSetMetaData.getColumnName(1)).thenReturn("Employee_Name");
        when(mockResultSet.getObject(1)).thenReturn("DataCloud");
    }


    @Test
    public void testGetConnection() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            String dbUrl = "jdbc:postgresql://localhost:5432/testdb";
            String user = "user";
            String password = "password";

            Connection connection = PostgreSQLUtilLib.getConnection(dbUrl, user, password);
            assertNotNull(connection);
            assertEquals(mockConnection, connection);
        }
    }

    @Test
    public void testExecuteQuery() throws SQLException {
        String query = "SELECT * FROM table";
        ResultSet resultSet = PostgreSQLUtilLib.executeQuery(mockConnection, query);
        assertNotNull(resultSet);
        verify(mockStatement, times(1)).executeQuery(query);
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        String update = "UPDATE table SET column = 'value' WHERE id = 1";
        int rowsAffected = PostgreSQLUtilLib.executeUpdate(mockConnection, update);
        assertEquals(1, rowsAffected);
        verify(mockStatement, times(1)).executeUpdate(update);
    }

    @Test
    public void testResultSetToList() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);

        List<Map<String, Object>> list = PostgreSQLUtilLib.resultSetToList(mockResultSet);
        assertEquals(1, list.size());
        assertEquals("DataCloud", list.get(0).get("Employee_Name"));
    }

    @Test
    public void testCommitTransaction() throws SQLException {
        PostgreSQLUtilLib.commitTransaction(mockConnection);
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testRollbackTransaction() throws SQLException {
        PostgreSQLUtilLib.rollbackTransaction(mockConnection);
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testCloseConnection() {
        PostgreSQLUtilLib.closeConnection(mockConnection);
        try {
            verify(mockConnection, times(1)).close();
        } catch (SQLException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    public void testCloseStatement() {
        PostgreSQLUtilLib.closeStatement(mockStatement);
        try {
            verify(mockStatement, times(1)).close();
        } catch (SQLException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    public void testCloseResultSet() {
        PostgreSQLUtilLib.closeResultSet(mockResultSet);
        try {
            verify(mockResultSet, times(1)).close();
        } catch (SQLException e) {
            fail("Exception should not be thrown");
        }
    }
}
