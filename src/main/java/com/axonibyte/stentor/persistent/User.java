/*
 * Copyright (c) 2020 V2C Development Team. All rights reserved.
 * Licensed under the Version 0.0.1 of the V2C License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at <https://tinyurl.com/v2c-license>.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions 
 * limitations under the License.
 */
package com.axonibyte.stentor.persistent;

import java.util.UUID;

import com.lambdaworks.crypto.SCryptUtil;

/**
 * Represents some user.
 * 
 * @author Caleb L. Power
 */
public class User {
  
  /**
   * Database key associated with the username.
   */
  public static final String USERNAME_KEY = "username";
  
  /**
   * Database key associated with the email.
   */
  public static final String EMAIL_KEY = "email";
  
  /**
   * Database key associated with the password hash.
   */
  public static final String PHASH_KEY = "phash";
  
  /**
   * Database key associated with the user's unique identifier.
   */
  public static final String ID_KEY = "id";
  
  private static String passwordSalt = "";
  
  private UUID id = null;
  private String email = null;
  private String username = null;
  private String pHash = null;
  
  /**
   * Sets the password salt to be used when building or verifying passwords.
   * 
   * @param passwordSalt the password salt
   */
  public static void setPasswordSalt(String passwordSalt) {
    User.passwordSalt = passwordSalt;
  }
  
  /**
   * Retrieves the user's unique ID.
   * 
   * @return the UUID associated with the user
   */
  public UUID getID() {
    return id;
  }
  
  /**
   * Sets the user's unique ID.
   * 
   * @param id the UUID associated with the user
   * @return this User
   */
  public User setID(UUID id) {
    this.id = id;
    return this;
  }
  
  /**
   * Retrieves the user's username.
   * 
   * @return the username associated with the user
   */
  public String getUsername() {
    return username;
  }
  
  /**
   * Sets the user's username.
   * 
   * @param username the username associated with the user
   * @return this User
   */
  public User setUsername(String username) {
    this.username = username;
    return this;
  }
  
  /**
   * Retrieves the user's email.
   * 
   * @return the email address associated with the user
   */
  public String getEmail() {
    return email;
  }
  
  /**
   * Sets the user's email.
   * 
   * @param email the email address associated with the user
   * @return this User
   */
  public User setEmail(String email) {
    this.email = email;
    return this;
  }
  
  /**
   * Retrieves the user's password hash.
   * 
   * @return the password hash associated with the user
   */
  public String getPasswordHash() {
    return pHash;
  }
  
  /**
   * Sets the user's password hash.
   * 
   * @param pHash the password hash associated with the user
   * @return this User
   */
  public User setPasswordHash(String pHash) {
    this.pHash = pHash;
    return this;
  }
  
  /**
   * Sets the user's password hash.
   * 
   * @param password the password associated with the user
   * @return this User
   */
  public User setPassword(String password) {
    return setPasswordHash(SCryptUtil.scrypt(password + passwordSalt, 16, 16, 16));
  }
  
  /**
   * Verifies a provided password against the saved passwor dhash.
   * 
   * @param password the provided password
   * @return <code>true</code> iff the password checks out
   */
  public boolean verifyPassword(String password) {
    try {
      return SCryptUtil.check(password + passwordSalt, pHash);
    } catch(IllegalArgumentException e) {
      e.printStackTrace();
      return false;
    }
  }
  
}
