/*
 * Copyright (c) 2021 Axonibyte Innovations, LLC
 * 
 *   Licensed under the Blue Oak Model License 1.0.0.
 *   See LICENSE for the full terms.
 */
package com.axonibyte.stentor.persistent;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link User} model.
 * 
 * @author Caleb L. Power
 */
public final class UserTest {
  
  private final User user = new User();
  
  /**
   * Tests the ID setter and getter.
   */
  @Test public void testSetAndGetID() {
    final UUID id = UUID.randomUUID();
    var returned = user.setID(id);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(returned, user);
  }
  
  /**
   * Tests the username setter and getter.
   */
  @Test public void testSetAndGetUsername() {
    final String username = "USERNAME";
    var returned = user.setUsername(username);
    Assert.assertEquals(user.getUsername(), username);
    Assert.assertEquals(returned, user);
  }
  
  /**
   * Tests the email setter and getter.
   */
  @Test public void testSetAndGetEmail() {
    final String email = "me@email.tld";
    var returned = user.setEmail(email);
    Assert.assertEquals(user.getEmail(), email);
    Assert.assertEquals(returned, user);
  }

  /**
   * Tests the password hash setter and getter.
   */
  @Test public void testSetAndGetPasswordHash() {
    final String pHash = "$s0$41010$dBbr+23Cn1pRt6N1cqi94w==$T6YLTAjl+J1l8p3KoHfygqFbwKcX1gy/4U9oSQFscdY=";
    var returned = user.setPasswordHash(pHash);
    Assert.assertEquals(user.getPasswordHash(), pHash);
    Assert.assertEquals(returned, user);
  }
  
  /**
   * Tests the password setter with the verify method.
   */
  @Test public void testSetPassword() {
    final String goodPassword = "secretTunnnellllll";
    final String badPassword = "throughTheMountainnnnsss";
    var returned = user.setPassword(goodPassword);
    Assert.assertFalse(user.verifyPassword(badPassword));
    Assert.assertTrue(user.verifyPassword(goodPassword));
    Assert.assertEquals(returned, user);
  }
  
  /**
   * Tests the verify method through the use of the password hash setter.
   */
  @Test public void testVerifyPassword() {
    final String goodPassword = "throughTheMountainnnnsss";
    final String badPassword = "secretTunnnellllll";
    final String pHash = "$s0$41010$Eic790wwEbij3NB7ruZyRw==$IAQiAnKLzDYOmZDZOFzhqvfjZDamq/+YmAxKmaXYZCg=";
    user.setPasswordHash(pHash);
    Assert.assertFalse(user.verifyPassword(badPassword));
    Assert.assertTrue(user.verifyPassword(goodPassword));
  }
  
}
