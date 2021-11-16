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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
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
  
  private static final String LOG_LABEL = "STENTOR CORE";
  
  private static final int DEFAULT_NETPORT = 2586;
  private static final int DEFAULT_DBPORT = 27017;
  private static final String DEFAULT_DBHOST = "127.0.0.1";
  private static final String DEFAULT_DBPROTO = "mongodb";
  private static final String DEFAULT_DBNAME = "stentor";
  private static final String DEFAULT_PASSWORD_SALT = "0a486beb-d953-4620-95c7-c99689fb228b";
  private static final String DEFAULT_PSK = "484dd6d1-9262-4975-a707-4238e08ed266";
  private static final String CONFIG_PARAM_LONG = "config-file";
  private static final String CONFIG_PARAM_SHORT = "c";
  private static final String ADD_ADMIN_SWITCH_LONG = "add-admin";
  private static final String ADD_ADMIN_SWITCH_SHORT = "a";
  private static final String RESET_PASSWORD_SWITCH_LONG = "reset-password";
  private static final String RESET_PASSWORD_SWITCH_SHORT = "r";
  private static final String PRINT_HELP_SWITCH_LONG = "help";
  private static final String PRINT_HELP_SWITCH_SHORT = "h";
  private static final String DEBUG_LOG_SWITCH_LONG = "debug";
  private static final String DEBUG_LOG_SWITCH_SHORT = "d";

  private static boolean debugEnabled = false; // true iff debug logging is enabled
  private static APIDriver aPIDriver = null; // the front end
  private static AuthTokenManager authTokenManager = null; // the auth token manager
  private static Database database = null; // the database
  
  /**
   * Entry point.
   * 
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    bootstrap(args);
  }
  
  /**
   * Bootstraps the program. This is distinct from the main method to make
   * testing a little bit easier.
   * 
   * @param args command-line arguments
   */
  public static void bootstrap(String[] args) {
    try {
      Options options = new Options();
      options.addOption(ADD_ADMIN_SWITCH_SHORT, ADD_ADMIN_SWITCH_LONG, false,
          "Adds an administrator and exits.");
      options.addOption(RESET_PASSWORD_SWITCH_SHORT, RESET_PASSWORD_SWITCH_LONG, false,
          "Resets a user's password.");
      options.addOption(CONFIG_PARAM_SHORT, CONFIG_PARAM_LONG, true,
          "Specifies the location of the configuration file. Default = NULL");
      options.addOption(PRINT_HELP_SWITCH_SHORT, PRINT_HELP_SWITCH_LONG, false,
          "Prints a useful help message.");
      options.addOption(DEBUG_LOG_SWITCH_SHORT, DEBUG_LOG_SWITCH_LONG, false,
          "Enables debug logging.");
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      
      debugEnabled = cmd.hasOption(DEBUG_LOG_SWITCH_LONG);
      
      if(cmd.hasOption(PRINT_HELP_SWITCH_LONG)) {
        final HelpFormatter formatter = new HelpFormatter();
        try(PrintWriter out = new PrintWriter(System.out)) {
          formatter.printHelp(out, 80, "stentor [ options... ]", "options:", options, 2, 4, "Copyright (c) 2021 Axonibyte Innovations, LLC");
          out.flush();
        }
        System.exit(0);
      }
      
      JSONObject config = null;
      if(cmd.hasOption(CONFIG_PARAM_LONG)) {
        String resource = readResource(cmd.getOptionValue(CONFIG_PARAM_LONG));
        if(resource == null) {
          try(InputStream in = Stentor.class.getResourceAsStream("/default-config.json");
              OutputStream out = new FileOutputStream(new File(cmd.getOptionValue(CONFIG_PARAM_LONG)))) {
            byte[] buf = new byte[4096];
            for(;;) {
              int len = in.read(buf);
              if(len == -1) break;
              out.write(buf, 0, len);
            }
          }
          throw new Exception("New config generated. Please configure it and try again.");
        } else {
          config = new JSONObject(resource);
        }
      }
      
      final JSONObject dbCfg = config != null ? config.optJSONObject("database") : null;
      final boolean dbSecure = dbCfg != null ? dbCfg.optBoolean("secure") : false;
      final int dbPort = dbCfg != null ? dbCfg.optInt("port") : 0;
      final String dbProto = dbCfg != null ? dbCfg.optString("protocol") : null;
      final String dbHost = dbCfg != null ? dbCfg.optString("host") : null;
      final String dbUser = dbCfg != null ? dbCfg.optString("username") : null;
      final String dbPass = dbCfg != null ? dbCfg.optString("password") : null;
      final String dbName = dbCfg != null ? dbCfg.getString("database") : null;
      
      final JSONObject authCfg = config != null ? config.optJSONObject("auth") : null;
      User.setPasswordSalt(authCfg != null && authCfg.has("salt") ? authCfg.getString("salt") : DEFAULT_PASSWORD_SALT);
      
      final JSONObject netCfg = config != null ? config.optJSONObject("net") : null;
      final int netPort = netCfg != null && netCfg.has("port") ? netCfg.getInt("port") : DEFAULT_NETPORT;
      final String netPSK = netCfg != null && netCfg.has("psk") ? netCfg.getString("psk") : DEFAULT_PSK;
      final String trustStore = netCfg != null && netCfg.has("truststore") ? netCfg.getString("truststore") : null;
      final String trustPass = netCfg != null && netCfg.has("trustpass")
          ? (netCfg.has("truststore") ? netCfg.getString("trustpass") : "") : null;
      
      if(trustStore != null) {
        Properties properties = System.getProperties();
        properties.setProperty("javax.net.ssl.trustStore", trustStore);
        properties.setProperty("javax.net.ssl.trustStorePassword", trustPass);
        System.setProperties(properties);
      }

      Logger.onInfo(LOG_LABEL, "Connecting to database...");
      
      database = new Database(
          dbProto != null ? dbProto : DEFAULT_DBPROTO,
          dbHost != null ? dbHost : DEFAULT_DBHOST,
          dbPort > 0 ? dbPort : DEFAULT_DBPORT,
          dbUser == null || dbPass == null ? null : dbUser,
          dbUser == null || dbPass == null ? null : dbPass,
          dbName != null ? dbName : DEFAULT_DBNAME,
          dbSecure);
      
      if(cmd.hasOption(ADD_ADMIN_SWITCH_LONG)) {
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
        
      } else if(cmd.hasOption(RESET_PASSWORD_SWITCH_LONG)) {
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
        aPIDriver = APIDriver.build(netPort, "*"); // configure the front end
        authTokenManager = new AuthTokenManager(netPSK);
    
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
      if(debugEnabled) e.printStackTrace();
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
    } catch(IOException e) {
      if(debugEnabled) e.printStackTrace();
    }
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
