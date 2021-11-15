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
package com.axonibyte.stentor.net.restful.user;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.net.restful.HTTPMethod;
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
      String email = request.getString(User.EMAIL_KEY);
      String username = request.getString(User.USERNAME_KEY);
      String password = request.getString(User.PASSWORD_KEY);
      
      if(Stentor.getDatabase().getUserProfileByEmail(email) != null)
        throw new EndpointException(req, "Email address conflict.", 409);
      
      if(Stentor.getDatabase().getUserProfileByUsername(username) != null)
        throw new EndpointException(req, "Username conflict.", 409);
      
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
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "User created.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
