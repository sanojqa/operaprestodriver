/*
 * Copyright 2011 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class CoreUtilsTest extends TestBase {

  // Make sure these tests only run if meta data is available.
  @Rule
  public MethodRule random = new MethodRule() {
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
      if (driver.services.getCoreUtils().hasMetaInformation()) {  // If meta info is exposed, return tests
        return base;
      } else {
        return new Statement() {  // otherwise return an empty statement -> test doesn't run

          @Override
          public void evaluate() throws Throwable {
          }
        };
      }
    }
  };

  private String coreVersion;
  private String os;
  private String product;
  private String binaryPath;
  private String userAgent;
  private int pid;

  @Before
  public void setUp() {
    coreVersion = driver.getCoreVersion();
    os = driver.getOS();
    product = driver.getProduct();
    binaryPath = driver.getBinaryPath();
    userAgent = driver.getUserAgent();
    pid = driver.getPID();
  }

  @Test
  public void testCoreVersion() {
    Assert.assertTrue(coreVersion.matches("\\d+\\.\\d+\\.\\d+"));
  }

  @Test
  public void testOS() {
    Assert.assertNotNull(os);
  }

  @Test
  public void testProductIsNotUnknown() {
    Assert.assertFalse(product.contains("unknown"));
  }

  @Test
  public void testProductIsKnown() {
    Assert.assertTrue(product.matches("core-gogi|desktop"));
  }

  @Test
  public void testBinaryPath() {
    Assert.assertNotNull(binaryPath);
  }

  @Test
  public void testUserAgent() {
    Assert.assertTrue(userAgent.matches("Opera/\\d+\\.\\d+.+"));
  }

  @Test
  public void testPID() {
    Assert.assertTrue(pid > 0);
  }

}