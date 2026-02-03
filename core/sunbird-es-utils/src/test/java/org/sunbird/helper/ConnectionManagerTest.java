package org.sunbird.helper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.keys.JsonKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectionManager.class})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*", "jdk.internal.reflect.*", "sun.security.ssl.*", "javax.crypto.*"})
public class ConnectionManagerTest {

  @Before
  public void setUp() {
    mockStatic(System.class);
  }

  @Test
  public void testGetRestClientSafe() {
    try {
      ConnectionManager.getRestClient();
    } catch (Exception e) {
      assertTrue("ConnectionManager.getRestClient() threw exception: " + e.getMessage(), false);
    }
  }
}
