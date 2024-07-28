package com.aisa.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MSSQLUtilLibTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetConnection() throws SQLException {
        // Arrange
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=testDB";
        String user = "testUser";
        String password = "testPassword";

        // Mock the DriverManager to return the mock connection
        mockStatic(DriverManager.class);
        when(DriverManager.getConnection(dbUrl, user, password)).thenReturn(mockConnection);

        // Act
        Connection connection = MSSQLUtilLib.getConnection(dbUrl, user, password);

        // Assert
        assertNotNull(connection);
        assertEquals(mockConnection, connection);
    }

    @Test
    public void testExecuteQuery() throws SQLException {
        String query = "SELECT * FROM testTable";
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(query)).thenReturn(mockResultSet);

        ResultSet resultSet = MSSQLUtilLib.executeQuery(mockConnection, query);
        assertNotNull(resultSet);
        verify(mockStatement, times(1)).executeQuery(query);
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        String update = "UPDATE testTable SET column1 = 'value' WHERE column2 = 'value2'";
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(update)).thenReturn(1);

        int rowsAffected = MSSQLUtilLib.executeUpdate(mockConnection, update);
        assertEquals(1, rowsAffected);
        verify(mockStatement, times(1)).executeUpdate(update);
    }

    @Test
    public void testResultSetToList() throws SQLException {
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(2);
        when(mockResultSetMetaData.getColumnName(1)).thenReturn("column1");
        when(mockResultSetMetaData.getColumnName(2)).thenReturn("column2");
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getObject(1)).thenReturn("value1");
        when(mockResultSet.getObject(2)).thenReturn("value2");

        List<Map<String, Object>> resultList = MSSQLUtilLib.resultSetToList(mockResultSet);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals("value1", resultList.get(0).get("column1"));
        assertEquals("value2", resultList.get(0).get("column2"));
    }

    @Test
    public void testCommitTransaction() throws SQLException {
        MSSQLUtilLib.commitTransaction(mockConnection);
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testRollbackTransaction() throws SQLException {
        MSSQLUtilLib.rollbackTransaction(mockConnection);
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testCloseConnection() throws SQLException {
        MSSQLUtilLib.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    public void testCloseStatement() throws SQLException {
        MSSQLUtilLib.closeStatement(mockStatement);
        verify(mockStatement, times(1)).close();
    }

    @Test
    public void testCloseResultSet() throws SQLException {
        MSSQLUtilLib.closeResultSet(mockResultSet);
        verify(mockResultSet, times(1)).close();
    }
}