package com.aisa.database;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DB2UtilityTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
    }

    @Test
    public void testGetConnection() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            String dbUrl = "jdbc:db2://localhost:50000/tests-data-cloud";
            String user = "data-cloud";
            String password = "data-cloud";
            Connection connection = PostgreSQLUtilLib.getConnection(dbUrl, user, password);
            assertNotNull(connection);
            assertEquals(mockConnection, connection);
        }
    }
    @Test
    public void testExecuteQuery() throws SQLException {
        String query = "SELECT * FROM SAMPLE_DATA_CLOUD_TABLE";
        when(mockStatement.executeQuery(query)).thenReturn(mockResultSet);

        ResultSet resultSet = DB2Utility.executeQuery(mockConnection, query);
        assertNotNull(resultSet);
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        String update = "UPDATE SAMPLE_TABLE SET COLUMN_NAME = 'value'";
        when(mockStatement.executeUpdate(update)).thenReturn(1);

        int rowsAffected = DB2Utility.executeUpdate(mockConnection, update);
        assertEquals(1, rowsAffected);
    }

    @Test
    public void testResultSetToList() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(1);
        when(mockResultSetMetaData.getColumnName(1)).thenReturn("COLUMN_NAME");
        when(mockResultSet.getObject(1)).thenReturn("value");

        List<Map<String, Object>> resultList = DB2Utility.resultSetToList(mockResultSet);
        assertEquals(1, resultList.size());
        assertEquals("value", resultList.get(0).get("COLUMN_NAME"));
    }

    @Test
    public void testCommitTransaction() throws SQLException {
        DB2Utility.commitTransaction(mockConnection);
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testRollbackTransaction() throws SQLException {
        DB2Utility.rollbackTransaction(mockConnection);
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, times(1)).setAutoCommit(true);
    }

    @Test
    public void testCloseConnection() throws SQLException {
        DB2Utility.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    public void testCloseStatement() throws SQLException {
        DB2Utility.closeStatement(mockStatement);
        verify(mockStatement, times(1)).close();
    }

    @Test
    public void testCloseResultSet() throws SQLException {
        DB2Utility.closeResultSet(mockResultSet);
        verify(mockResultSet, times(1)).close();
    }
}