/*
 * Copyright (c) 2021 Axonibyte Innovations, LLC. All rights reserved.
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   https://apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the license.
 */
package com.axonibyte.stentor.net.auth;

import java.lang.reflect.Field;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axonibyte.stentor.persistent.User;

/**
 * Tests {@link AuthToken}.
 * 
 * @author Caleb L. Power
 */
public final class AuthTokenTest {
  
  /**
   * Tests {@link AuthToken#checkN00bStatus()} to ensure that only the first
   * return value of this method is {@code true}. Subsequent returns should be
   * {@code false}. Note: obviously this cannot be deterministically checked
   * for truthiness, so checking "a few more times" is good enough for
   * government work.
   */
  @Test public void testCheckN00bStatus() {
    var authToken = new AuthToken();
    Assert.assertTrue(authToken.checkN00bStatus());
    Assert.assertFalse(authToken.checkN00bStatus());
    Assert.assertFalse(authToken.checkN00bStatus());
    Assert.assertFalse(authToken.checkN00bStatus());
  }
  
  /**
   * Tests {@link AuthToken#setUser(User)} and {@link AuthToken#getUser()}.
   */
  @Test public void testSetAndGetUser() {
    User user = EasyMock.mock(User.class);
    EasyMock.replay(user);
    var authToken = new AuthToken();
    authToken.setUser(user);
    Assert.assertEquals(authToken.getUser(), user);
    EasyMock.verify(user);
  }
  
  /**
   * Tests {@link AuthToken#setClientIP(String)}
   * and {@link AuthToken#getClientIP()}.
   */
  @Test public void testSetAndGetClientIP() {
    String clientIP = "1.2.3.4";
    var authToken = new AuthToken();
    var returned = authToken.setClientIP(clientIP);
    Assert.assertEquals(authToken.getClientIP(), clientIP);
    Assert.assertEquals(returned, authToken);
  }
  
  /**
   * Tests {@link AuthToken#setSessionKey(String)}
   * and {@link AuthToken#getSessionKey()}.
   */
  @Test public void testSetAndGetSessionKey() {
    String sessionKey = "THIS_IS_A_SESSION_KEY_MAYBE";
    var authToken = new AuthToken();
    var returned = authToken.setSessionKey(sessionKey);
    Assert.assertEquals(authToken.getSessionKey(), sessionKey);
    Assert.assertEquals(returned, authToken);
  }
  
  /**
   * Tests {@link AuthToken#hasClientPerms()}. This should be {@code true} iff
   * the user has been set and is not {@code null}.
   */
  @Test public void testHasClientPerms() {
    User user = EasyMock.mock(User.class);
    EasyMock.replay(user);
    var authToken = new AuthToken();
    Assert.assertFalse(authToken.hasClientPerms());
    authToken.setUser(user);
    Assert.assertTrue(authToken.hasClientPerms());
    EasyMock.verify(user);
  }
  
  /**
   * Tests {@link AuthToken#hasAdminPerms()}. This should be {@code true} iff
   * the user has been set and is not {@code null}. Note: all clients are
   * currently admins. This might change in the future.
   */
  @Test public void testHasAdminPerms() {
    User user = EasyMock.mock(User.class);
    EasyMock.replay(user);
    var authToken = new AuthToken();
    Assert.assertFalse(authToken.hasAdminPerms());
    authToken.setUser(user);
    Assert.assertTrue(authToken.hasAdminPerms());
    EasyMock.verify(user);
  }
  
  /**
   * Tests {@link AuthToken#hasExpired()}. This is done by manually setting the
   * the last access timestamp to one (1) second before and after the
   * expiration time. Now, obviously this introduces a race condition (and is
   * arguably not the best practice in testing). However, it's assumed that if
   * your toaster takes more than a second to execute this test method, you're
   * probably going to have issues running Gradle anyhow.
   * 
   * @throws Exception if private fields couldn't be modified via Reflection
   */
  @Test public void testHasExpired() throws Exception {
    var authToken = new AuthToken();
    Field timestamp = authToken.getClass().getDeclaredField("lastAccessTimestamp");
    timestamp.setAccessible(true);
    timestamp.set(authToken, System.currentTimeMillis() - 1000 * 60 * 15 + 1000);
    Assert.assertFalse(authToken.hasExpired());
    timestamp.set(authToken, System.currentTimeMillis() - 1000 * 60 * 15 - 1000);
    Assert.assertTrue(authToken.hasExpired());
  }
  
  /**
   * Tests {@link AuthToken#bump()}. This is done by checking the current clock
   * before and after the timestamp is bumped.
   * 
   * @throws Exception if private fields couldn't be accessed via Reflection
   */
  @Test public void testBump() throws Exception {
    var authToken = new AuthToken();
    Field timestamp = authToken.getClass().getDeclaredField("lastAccessTimestamp");
    timestamp.setAccessible(true);
    long stop1 = System.currentTimeMillis();
    Assert.assertTrue(timestamp.getLong(authToken) <= stop1);
    authToken.bump();
    long stop2 = System.currentTimeMillis();
    Assert.assertTrue(timestamp.getLong(authToken) >= stop1);
    Assert.assertTrue(timestamp.getLong(authToken) <= stop2);
  }
  
}
