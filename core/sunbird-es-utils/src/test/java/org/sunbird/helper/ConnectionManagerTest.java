package org.sunbird.helper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConnectionManagerTest {

  @Test
  public void testGetRestClientSafe() {
    // Since we are not setting up the environment variables, the connection manager
    // should fail to initialize the client, but it should not throw an exception.
    // It might return null or keep the client null.
    try {
      ConnectionManager.getRestClient();
    } catch (Exception e) {
      // If it throws exception, that's bad.
      assertTrue("ConnectionManager.getRestClient() threw exception: " + e.getMessage(), false);
    }
  }
}
