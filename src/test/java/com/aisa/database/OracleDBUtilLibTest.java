package com.aisa.database;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleDBUtilLibTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String username = "your_username";
        String password = "your_password";

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(url, username, password))
                    .thenReturn(mockConnection);

            Connection connection = OracleDBUtilLib.getConnection(url, username, password);
            assertNotNull(connection);
        }
    }

    @Test
    void testExecuteQuery() throws SQLException {
        String query = "SELECT * FROM your_table";

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(query)).thenReturn(mockResultSet);

        ResultSet resultSet = OracleDBUtilLib.executeQuery(mockConnection, query);
        assertNotNull(resultSet);
        verify(mockConnection, times(1)).createStatement();
        verify(mockStatement, times(1)).executeQuery(query);
    }

    @Test
    void testExecuteUpdate() throws SQLException {
        String query = "UPDATE your_table SET name='test' WHERE id=1";

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(query)).thenReturn(1);

        int rowsAffected = OracleDBUtilLib.executeUpdate(mockConnection, query);
        assertEquals(1, rowsAffected);
        verify(mockConnection, times(1)).createStatement();
        verify(mockStatement, times(1)).executeUpdate(query);
    }

    @Test
    void testCloseConnection() throws SQLException {
        OracleDBUtilLib.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    void testCloseResultSet() throws SQLException {
        OracleDBUtilLib.closeResultSet(mockResultSet);
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void testCloseStatement() throws SQLException {
        OracleDBUtilLib.closeStatement(mockStatement);
        verify(mockStatement, times(1)).close();
    }

    @Test
    void testMapResultSetToPOJO() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(2);
        when(mockResultSetMetaData.getColumnName(1)).thenReturn("id");
        when(mockResultSetMetaData.getColumnName(2)).thenReturn("name");
        when(mockResultSet.getObject(1)).thenReturn(0);
        when(mockResultSet.getObject(2)).thenReturn("Test Name");

        List<OracleDBUtilLib.ExamplePOJO> resultList = OracleDBUtilLib.mapResultSetToPOJO(mockResultSet, OracleDBUtilLib.ExamplePOJO.class);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(0, resultList.get(0).getId());
        assertEquals("Test Name", resultList.get(0).getName());
    }
}