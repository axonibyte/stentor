/*
 * Copyright (c) 2020 Axonibyte Innovations, LLC. All rights reserved.
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
package com.axonibyte.stentor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.json.JSONObject;

import com.axonibyte.stentor.log.Logger;
import com.axonibyte.stentor.net.APIDriver;
import com.axonibyte.stentor.net.auth.AuthTokenManager;
import com.axonibyte.stentor.persistent.Database;
import com.axonibyte.stentor.persistent.User;

/**
 * Stentor minimal blog backend.
 * 
 * @author Caleb L. Power
 */
public class Stentor {
  
  private static final String LOG_LABEL = "DISPATCHER CORE";
  
  private static final int DEFAULT_PORT = 2586;
  private static final String DEFAULT_DATABASE = "127.0.0.1:27017";
  private static final String DEFAULT_PASSWORD_SALT = "0a486beb-d953-4620-95c7-c99689fb228b";
  private static final String DEFAULT_PSK = "484dd6d1-9262-4975-a707-4238e08ed266";
  private static final String CONFIG_PARAM_LONG = "config-file";
  private static final String CONFIG_PARAM_SHORT = "c";
  private static final String DB_PARAM_LONG = "database";
  private static final String DB_PARAM_SHORT = "d";
  private static final String PORT_PARAM_LONG = "port";
  private static final String PORT_PARAM_SHORT = "p";
  private static final String PASSWORD_SALT_PARAM_LONG = "password-salt";
  private static final String PASSWORD_SALT_PARAM_SHORT = "s";
  private static final String PSK_PARAM_LONG = "preshared-key";
  private static final String PSK_PARAM_SHORT = "k";
  private static final String ADD_ADMIN_PARAM_LONG = "add-admin";
  private static final String ADD_ADMIN_PARAM_SHORT = "a";
  private static final String RESET_PASSWORD_PARAM_LONG = "reset-password";
  private static final String RESET_PASSWORD_PARAM_SHORT = "r";

  private static APIDriver aPIDriver = null; // the front end
  private static AuthTokenManager authTokenManager = null; // the auth token manager
  private static Database database = null; // the database
  
  /**
   * Entry point.
   * 
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    try {
      Options options = new Options();
      options.addOption(ADD_ADMIN_PARAM_SHORT, ADD_ADMIN_PARAM_LONG, false,
          "Adds an administrator and exits.");
      options.addOption(RESET_PASSWORD_PARAM_SHORT, RESET_PASSWORD_PARAM_LONG, false,
          "Resets a user's password.");
      options.addOption(CONFIG_PARAM_SHORT, CONFIG_PARAM_LONG, true,
          "Specifies the location of the configuration file. Default = NULL");
      options.addOption(DB_PARAM_SHORT, DB_PARAM_LONG, true,
          "Specifies the target database server. Default = " + DEFAULT_DATABASE);
      options.addOption(PORT_PARAM_SHORT, PORT_PARAM_LONG, true,
          "Specifies the server's listening port. Default = " + DEFAULT_PORT);
      options.addOption(PSK_PARAM_SHORT, PSK_PARAM_LONG, true,
          "Specifies the preshared key for authentication. Default = " + DEFAULT_PSK);
      options.addOption(PASSWORD_SALT_PARAM_SHORT, PASSWORD_SALT_PARAM_LONG, true,
          "Specified the salt used to build user password hashes. Default = " + DEFAULT_PASSWORD_SALT);
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      
      JSONObject config = null;
      if(cmd.hasOption(CONFIG_PARAM_LONG))
        config = new JSONObject(readResource(cmd.getOptionValue(CONFIG_PARAM_LONG)));
      
      final int port = cmd.hasOption(PORT_PARAM_LONG)
          ? Integer.parseInt(cmd.getOptionValue(PORT_PARAM_LONG))
              : (config != null && config.has(PORT_PARAM_LONG)
                  ? config.getInt(PORT_PARAM_LONG)
                      : DEFAULT_PORT);
          
      final String dbConnection = cmd.hasOption(DB_PARAM_LONG)
          ? cmd.getOptionValue(DB_PARAM_LONG)
              : (config != null && config.has(DB_PARAM_LONG)
                  ? config.getString(DB_PARAM_LONG)
                      : DEFAULT_DATABASE);
            
      final String psk = cmd.hasOption(PSK_PARAM_LONG)
          ? cmd.getOptionValue(PSK_PARAM_LONG)
              : (config != null && config.has(PSK_PARAM_LONG)
                  ? config.getString(PSK_PARAM_LONG)
                      : DEFAULT_PSK);
          
      User.setPasswordSalt(cmd.hasOption(PASSWORD_SALT_PARAM_LONG)
          ? cmd.getOptionValue(PASSWORD_SALT_PARAM_LONG)
              : (config != null && config.has(PASSWORD_SALT_PARAM_LONG)
                  ? config.getString(PASSWORD_SALT_PARAM_LONG)
                      : DEFAULT_PASSWORD_SALT));

      Logger.onInfo(LOG_LABEL, "Connecting to database...");
      database = new Database(dbConnection);
      
      if(cmd.hasOption(ADD_ADMIN_PARAM_LONG)) {
        User user = new User();
        
        for(;;) {
          String username = new String(System.console().readLine("Enter username: "));
          if(Stentor.getDatabase().getUserProfileByUsername(username) == null) {
            user.setUsername(username);
            break;
          }
          System.out.println("That username already exists in the database!");
        }
        
        for(;;) {
          String email = new String(System.console().readLine("Enter email: "));
          if(Stentor.getDatabase().getUserProfileByEmail(email) == null) {
            user.setEmail(email);
            break;
          }
          System.out.println("That email already exists in the database!");
        }
        
        String password = new String(System.console().readPassword("Enter password: "));
        user.setPassword(password);
        
        UUID uuid = null;
        do uuid = UUID.randomUUID();
        while(Stentor.getDatabase().getUserProfileByID(uuid) != null);
        user.setID(uuid);
        
        database.setUserProfile(user);
        System.out.println("User created.");
        
      } else if(cmd.hasOption(RESET_PASSWORD_PARAM_LONG)) {
        String username = new String(System.console().readLine("Enter username: "));
        User user = database.getUserProfileByUsername(username);
        if(user == null)
          System.out.println("That user doesn't exist.");
        else {
          String password = new String(System.console().readPassword("Enter password: "));
          user.setPassword(password);
          database.setUserProfile(user);
          System.out.println("User saved!");
        }
      } else {
        Logger.onInfo(LOG_LABEL, "Spinning up API driver...");
        aPIDriver = APIDriver.build(port, "*"); // configure the front end
        authTokenManager = new AuthTokenManager(psk);
    
        // catch CTRL + C
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override public void run() {
            Logger.onInfo(LOG_LABEL, "Shutting off API driver...");
            aPIDriver.halt();
            Logger.onInfo(LOG_LABEL, "Goodbye! ^_^");
          }
        });
      }
    } catch(Exception e) {
      Logger.onError(LOG_LABEL, "Some exception was thrown during launch: " + e.getMessage());
    }
  }
  
  /**
   * Reads a resource, preferably plaintext. The resource can be in the
   * classpath, in the JAR (if compiled as such), or on the disk. <em>Reads the
   * entire file at once--so it's probably not wise to read huge files at one
   * time.</em> Eliminates line breaks in the process, so best for source files
   * i.e. HTML or SQL.
   * 
   * @param resource the file that needs to be read
   * @return String containing the file's contents
   */
  public static String readResource(String resource) {
    try {
      if(resource == null) return null;
      File file = new File(resource);
      InputStream inputStream = null;
      if(file.canRead())
        inputStream = new FileInputStream(file);
      else
        inputStream = Stentor.class.getResourceAsStream(resource);
      if(inputStream == null) return null;
      InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(streamReader);
      StringBuilder stringBuilder = new StringBuilder();
      for(String line; (line = reader.readLine()) != null;)
        stringBuilder.append(line.trim());
      return stringBuilder.toString();
    } catch(IOException e) { }
    return null;
  }
  
  /**
   * Retrieves the database connection.
   * 
   * @return the database connection
   */
  public static Database getDatabase() {
    return database;
  }
  
  /**
   * Retrieves the authentication token manager.
   * 
   * @return the auth token manager
   */
  public static AuthTokenManager getAuthTokenManager() {
    return authTokenManager;
  }
  
}
