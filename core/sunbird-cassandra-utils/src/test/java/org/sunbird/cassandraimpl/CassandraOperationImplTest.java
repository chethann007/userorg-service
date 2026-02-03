package org.sunbird.cassandraimpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sunbird.common.CassandraPropertyReader;
import org.sunbird.common.Constants;
import org.sunbird.helper.CassandraConnectionManager;
import org.sunbird.helper.CassandraConnectionMngrFactory;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

@RunWith(MockitoJUnitRunner.class)
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
  private CassandraPropertyReader propertyReader;

  @Before
  public void setUp() throws Exception {
    // Note: Mocks are initialized by MockitoJUnitRunner

    // Inject Mock ConnectionManager into Factory using Reflection
    setSingletonInstance(CassandraConnectionMngrFactory.class, "instance", connectionManager);

    // Inject Mock PropertyReader into Factory using Reflection
    setSingletonInstance(CassandraPropertyReader.class, "cassandraPropertyReader", propertyReader);
    when(propertyReader.readProperty(anyString())).thenAnswer(i -> i.getArgument(0));
    when(propertyReader.readPropertyValue(anyString())).thenAnswer(i -> i.getArgument(0));

    // Initialize concrete implementation
    cassandraOperation = new CassandraOperationImplConcrete();

    // Setup basic session behavior
    when(connectionManager.getSession(anyString())).thenReturn(session);
    when(session.prepare(anyString())).thenReturn(preparedStatement);

    // Setup PreparedStatement to allow BoundStatement creation (real driver code)
    // BoundStatement constructor calls getVariables()
    when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
    when(columnDefinitions.size()).thenReturn(10); // Mock size for arbitrary columns

    // Mock execution
    when(session.execute(any(BoundStatement.class))).thenReturn(resultSet);
    when(session.execute(any(Statement.class))).thenReturn(resultSet);

    // Setup ResultSet to return success
    when(resultSet.iterator()).thenReturn(Collections.emptyIterator());
    when(resultSet.getColumnDefinitions()).thenReturn(columnDefinitions);
    when(columnDefinitions.asList()).thenReturn(Collections.emptyList());
  }

  private void setSingletonInstance(Class<?> clazz, String fieldName, Object instance) throws Exception {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(null, instance);
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
    verify(session, times(1)).execute(any(Statement.class));
  }

  @Test
  public void testGetRecordById() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String identifier = "123";

      Response response = cassandraOperation.getRecordById(keyspaceName, tableName, identifier, requestContext);

      // Response construction logic in CassandraUtil.createResponse uses resultSet iterator
      // We mocked iterator to be empty, so response list should be empty but success
      List<?> result = (List<?>) response.get(Constants.RESPONSE);
      assertEquals(0, result.size());
  }

  @Test
  public void testGetRecordsByProperty() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String propertyName = "name";
      String propertyValue = "John";

      Response response = cassandraOperation.getRecordsByProperty(keyspaceName, tableName, propertyName, propertyValue, requestContext);

      List<?> result = (List<?>) response.get(Constants.RESPONSE);
      assertEquals(0, result.size());
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
      verify(session, times(1)).execute(any(Statement.class));
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
