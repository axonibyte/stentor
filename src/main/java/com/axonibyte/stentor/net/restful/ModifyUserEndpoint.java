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
 * Endpoint to handle user modifications.
 * 
 * @author Caleb L. Power
 */
public class ModifyUserEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public ModifyUserEndpoint() {
    super("/users/:user", APIVersion.VERSION_1, HTTPMethod.PATCH);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res); // require user to be logged in
    
    try {
      UUID id = null;
      
      try {
        id = UUID.fromString(req.params("user"));
      } catch(IllegalArgumentException e) { }
      
      User user = null;
      user = id == null ? null : Stentor.getDatabase().getUserProfileByID(id);
      if(user == null) throw new EndpointException(req, "User not found.", 404);
      
      if(!authToken.getUser().getID().equals(id))
        throw new EndpointException(req, "Access denied.", 403);
      
      JSONObject request = new JSONObject(req.body());
      String email = request.has(User.EMAIL_KEY) ? request.getString(User.EMAIL_KEY) : user.getEmail();
      String username = request.has(User.USERNAME_KEY) ? request.getString(User.USERNAME_KEY) : user.getUsername();
      String password = request.has(User.PASSWORD_KEY) ? request.getString(User.PASSWORD_KEY) : null;
      
      if(!email.equalsIgnoreCase(user.getEmail())
          && Stentor.getDatabase().getUserProfileByEmail(email) != null)
        throw new EndpointException(req, "Email already exists.", 409);
      
      if(!username.equalsIgnoreCase(user.getUsername())
          && Stentor.getDatabase().getUserProfileByUsername(username) != null)
        throw new EndpointException(req, "Username already exists.", 409);
      
      if(password != null) user.setPassword(password);
      user.setEmail(email).setUsername(username);
      
      Stentor.getDatabase().setUserProfile(user);
      
      res.status(202);
      return new JSONObject()
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "User updated.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
