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
package com.axonibyte.stentor.net.restful;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.persistent.User;

import spark.Request;
import spark.Response;

/**
 * Endpoint to handle user creation.
 * 
 * @author Caleb L. Power
 */
public class CreateUserEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public CreateUserEndpoint() {
    super("/users", APIVersion.VERSION_1, HTTPMethod.POST);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res); // require user to be logged in
    
    try {
      JSONObject request = new JSONObject(req.body());
      String email = request.getString("email");
      String username = request.getString("username");
      String password = request.getString("password");
      
      if(Stentor.getDatabase().getUserProfileByEmail(email) != null)
        throw new EndpointException(req, "Email already exists.", 409);
      
      if(Stentor.getDatabase().getUserProfileByUsername(username) != null)
        throw new EndpointException(req, "Username already exists.", 409);
      
      UUID uuid = null;
      do uuid = UUID.randomUUID();
      while(Stentor.getDatabase().getUserProfileByID(uuid) != null);
      
      Stentor.getDatabase().setUserProfile(new User()
          .setEmail(email)
          .setUsername(username)
          .setPassword(password)
          .setID(uuid));
      
      res.status(201);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "User created.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
