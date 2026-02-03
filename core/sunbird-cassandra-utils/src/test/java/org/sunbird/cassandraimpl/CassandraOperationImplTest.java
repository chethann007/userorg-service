package org.sunbird.cassandraimpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.CassandraUtil;
import org.sunbird.common.Constants;
import org.sunbird.helper.CassandraConnectionManager;
import org.sunbird.helper.CassandraConnectionMngrFactory;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.response.Response;
import org.sunbird.request.RequestContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    CassandraOperationImpl.class,
    CassandraUtil.class,
    CassandraConnectionMngrFactory.class,
    LoggerUtil.class
})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*", "jdk.internal.reflect.*", "javax.crypto.*", "javax.script.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "org.xml.*"})
@SuppressStaticInitializationFor({"org.sunbird.common.CassandraUtil", "org.sunbird.common.CassandraPropertyReader"})
public class CassandraOperationImplTest {

  private CassandraOperationImpl cassandraOperation;

  @Mock
  private CassandraConnectionManager connectionManager;

  @Mock
  private Session session;

  @Mock
  private PreparedStatement preparedStatement;

  @Mock
  private ColumnDefinitions columnDefinitions;

  @Mock
  private ResultSet resultSet;

  @Mock
  private RequestContext requestContext;

  @Mock
  private LoggerUtil loggerUtil;

  @Before
  public void setUp() throws Exception {
    connectionManager = mock(CassandraConnectionManager.class);
    session = mock(Session.class);
    preparedStatement = mock(PreparedStatement.class);
    columnDefinitions = mock(ColumnDefinitions.class);
    resultSet = mock(ResultSet.class);
    loggerUtil = mock(LoggerUtil.class);

    // Mock CassandraConnectionMngrFactory.getInstance() BEFORE creating the instance
    PowerMockito.mockStatic(CassandraConnectionMngrFactory.class);
    when(CassandraConnectionMngrFactory.getInstance()).thenReturn(connectionManager);

    // Mock LoggerUtil constructor
    PowerMockito.whenNew(LoggerUtil.class).withAnyArguments().thenReturn(loggerUtil);

    // Initialize concrete implementation
    cassandraOperation = new CassandraOperationImplConcrete();

    // Setup basic session behavior
    when(connectionManager.getSession(anyString())).thenReturn(session);
    when(session.prepare(anyString())).thenReturn(preparedStatement);
    when(session.prepare(any(com.datastax.driver.core.RegularStatement.class))).thenReturn(preparedStatement);

    // Setup PreparedStatement to allow BoundStatement creation
    when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
    when(columnDefinitions.size()).thenReturn(10); // Mock size for arbitrary columns

    // Mock execution
    when(session.execute(any(BoundStatement.class))).thenReturn(resultSet);
    when(session.execute(any(com.datastax.driver.core.Statement.class))).thenReturn(resultSet);

    // Mock CassandraUtil static methods
    PowerMockito.mockStatic(CassandraUtil.class);
    when(CassandraUtil.getPreparedStatement(anyString(), anyString(), anyMap())).thenReturn("INSERT INTO ...");
    when(CassandraUtil.getUpdateQueryStatement(anyString(), anyString(), anyMap())).thenReturn("UPDATE ...");
    when(CassandraUtil.getSelectStatement(anyString(), anyString(), any(List.class))).thenReturn("SELECT ...");

    // Mock createResponse to return a valid Response object
    Response mockResponse = new Response();
    mockResponse.put(Constants.RESPONSE, Constants.SUCCESS);
    when(CassandraUtil.createResponse(resultSet)).thenReturn(mockResponse);
  }

  @Test
  public void testInsertRecord() {
    String keyspaceName = "sunbird";
    String tableName = "user";
    Map<String, Object> request = new HashMap<>();
    request.put("id", "123");
    request.put("name", "John");

    Response response = cassandraOperation.insertRecord(keyspaceName, tableName, request, requestContext);

    assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
    verify(session, times(1)).execute(any(BoundStatement.class));
  }

  @Test
  public void testUpdateRecord() {
    String keyspaceName = "sunbird";
    String tableName = "user";
    Map<String, Object> request = new HashMap<>();
    request.put("id", "123");
    request.put("name", "John");

    when(CassandraUtil.getUpdateQueryStatement(anyString(), anyString(), anyMap()))
        .thenReturn("UPDATE sunbird.user SET name = ? where id = ?;");

    Response response = cassandraOperation.updateRecord(keyspaceName, tableName, request, requestContext);

    assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
    verify(session, times(1)).execute(any(BoundStatement.class));
  }

  @Test
  public void testDeleteRecord() {
    String keyspaceName = "sunbird";
    String tableName = "user";
    String identifier = "123";

    Response response = cassandraOperation.deleteRecord(keyspaceName, tableName, identifier, requestContext);

    assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
    verify(session, times(1)).execute(any(com.datastax.driver.core.Statement.class));
  }

  @Test
  public void testGetRecordById() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String identifier = "123";

      Response response = cassandraOperation.getRecordById(keyspaceName, tableName, identifier, requestContext);

      assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
  }

  @Test
  public void testGetRecordsByProperty() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String propertyName = "name";
      String propertyValue = "John";

      Response response = cassandraOperation.getRecordsByProperty(keyspaceName, tableName, propertyName, propertyValue, requestContext);

      assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
  }

  @Test
  public void testBatchInsert() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      List<Map<String, Object>> records = new ArrayList<>();
      Map<String, Object> record1 = new HashMap<>();
      record1.put("id", "1");
      records.add(record1);

      Response response = cassandraOperation.batchInsert(keyspaceName, tableName, records, requestContext);

      assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
  }

  // Concrete implementation for testing
  private static class CassandraOperationImplConcrete extends CassandraOperationImpl {
    @Override
    public Response getRecordsWithLimit(String keyspace, String table, Map<String, Object> filters, List<String> fields, Integer limit, RequestContext requestContext) {
        return null;
    }

    @Override
    public Response updateAddMapRecord(String keySpace, String table, Map<String, Object> primaryKey, String column, String key, Object value, RequestContext requestContext) {
        return null;
    }

    @Override
    public Response updateRemoveMapRecord(String keySpace, String table, Map<String, Object> primaryKey, String column, String key, RequestContext requestContext) {
        return null;
    }
  }
}
