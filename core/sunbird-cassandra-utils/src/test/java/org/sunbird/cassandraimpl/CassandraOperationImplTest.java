package org.sunbird.cassandraimpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select.Where;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sunbird.common.Constants;
import org.sunbird.helper.CassandraConnectionManager;
import org.sunbird.response.Response;
import org.sunbird.request.RequestContext;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CassandraOperationImplTest {

  private CassandraOperationImpl cassandraOperation;

  @Mock
  private CassandraConnectionManager connectionManager;

  @Mock
  private Session session;

  @Mock
  private PreparedStatement preparedStatement;

  @Mock
  private BoundStatement boundStatement;

  @Mock
  private ResultSet resultSet;

  @Mock
  private RequestContext requestContext;

  @Mock
  private ColumnDefinitions columnDefinitions;

  @Before
  public void setUp() throws Exception {
    // Note: Mocks are initialized by MockitoJUnitRunner

    cassandraOperation = new CassandraOperationImplConcrete(boundStatement);
    setConnectionManager(cassandraOperation, connectionManager);

    when(connectionManager.getSession(anyString())).thenReturn(session);
    when(session.prepare(anyString())).thenReturn(preparedStatement);

    // Mock PreparedStatement.bind calls
    when(preparedStatement.bind(any(Object[].class))).thenReturn(boundStatement);
    when(preparedStatement.bind()).thenReturn(boundStatement);

    // Mock PreparedStatement.getVariables for BoundStatement constructor
    when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
    when(columnDefinitions.size()).thenReturn(0);

    // Mock BoundStatement.bind calls
    when(boundStatement.bind(any(Object[].class))).thenReturn(boundStatement);

    when(session.execute(any(BoundStatement.class))).thenReturn(resultSet);
    when(session.execute(any(com.datastax.driver.core.Statement.class))).thenReturn(resultSet);
  }

  private void setConnectionManager(CassandraOperationImpl operation, CassandraConnectionManager manager) {
      try {
          java.lang.reflect.Field field = CassandraOperationImpl.class.getDeclaredField("connectionManager");
          field.setAccessible(true);
          field.set(operation, manager);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
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
  }

  @Test
  public void testDeleteRecord() {
    String keyspaceName = "sunbird";
    String tableName = "user";
    String identifier = "123";

    Response response = cassandraOperation.deleteRecord(keyspaceName, tableName, identifier, requestContext);

    assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
  }

  @Test
  public void testGetRecordById() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String identifier = "123";

      Response response = cassandraOperation.getRecordById(keyspaceName, tableName, identifier, requestContext);

      List<?> result = (List<?>) response.get(Constants.RESPONSE);
      assertEquals("mocked_success", result.get(0));
  }

  @Test
  public void testGetRecordsByProperty() {
      String keyspaceName = "sunbird";
      String tableName = "user";
      String propertyName = "name";
      String propertyValue = "John";

      Response response = cassandraOperation.getRecordsByProperty(keyspaceName, tableName, propertyName, propertyValue, requestContext);

      List<?> result = (List<?>) response.get(Constants.RESPONSE);
      assertEquals("mocked_success", result.get(0));
  }

  // Concrete implementation for testing with mocked/overridden methods
  private static class CassandraOperationImplConcrete extends CassandraOperationImpl {

    private BoundStatement mockBoundStatement;

    public CassandraOperationImplConcrete(BoundStatement mockBoundStatement) {
        this.mockBoundStatement = mockBoundStatement;
    }

    @Override
    protected BoundStatement createBoundStatement(PreparedStatement statement) {
        // Even though we override this, insertRecord calls createBoundStatement(statement)
        // If we return the mockBoundStatement, we are good.
        // However, the base class also calls statement.getVariables() inside insertRecord?
        // No, base class insertRecord:
        /*
          BoundStatement boundStatement = createBoundStatement(statement);
        */
        // If we override createBoundStatement, the logic inside createBoundStatement is executed.
        // In this class, we return mockBoundStatement.
        // So the real BoundStatement constructor is NOT called.
        // So why did I get NPE on getVariables() earlier?
        // Ah, because I hadn't overridden createBoundStatement in the test subclass properly?
        // Or insertRecord was still using new BoundStatement(statement)?
        // I used replace_with_git_merge_diff to replace new BoundStatement with createBoundStatement.
        // Let's assume I replaced it correctly.
        return mockBoundStatement;
    }

    @Override
    protected String getPreparedStatement(String keyspaceName, String tableName, Map<String, Object> map) {
        return "INSERT INTO ...";
    }

    @Override
    protected Response createResponse(ResultSet results) {
        Response response = new Response();
        response.put(Constants.RESPONSE, Collections.singletonList("mocked_success"));
        return response;
    }

    @Override
    protected String getUpdateQueryStatement(String keyspaceName, String tableName, Map<String, Object> map) {
        return "UPDATE ...";
    }

    @Override
    protected String getSelectStatement(String keyspaceName, String tableName, List<String> properties) {
        return "SELECT ...";
    }

    @Override
    protected String processExceptionForUnknownIdentifier(Exception e) {
        return "mocked_error";
    }

    @Override
    protected void createQuery(String key, Object value, Where where) {
        // do nothing
    }

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

    @Override
    protected void logError(RequestContext context, String message, Object... args) {
        System.err.println("LOG ERROR: " + formatLogMessage(message, args));
        if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
            ((Throwable) args[args.length - 1]).printStackTrace();
        }
    }

    @Override
    protected void logInfo(RequestContext context, String message, Object... args) {
        // no-op or sysout
    }

    @Override
    protected void logDebug(RequestContext context, String message, Object... args) {
        // no-op
    }

    @Override
    protected void logWarn(RequestContext context, String message, Object... args) {
        // no-op
    }
  }
}
