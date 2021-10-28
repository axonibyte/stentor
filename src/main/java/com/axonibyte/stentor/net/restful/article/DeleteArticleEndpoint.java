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
package com.axonibyte.stentor.net.restful.article;

import java.util.UUID;

import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.net.restful.HTTPMethod;

import spark.Request;
import spark.Response;

/**
 * Endpoint to handle article deletion.
 * 
 * @author Caleb L. Power
 */
public class DeleteArticleEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public DeleteArticleEndpoint() {
    super("/articles/:article", APIVersion.VERSION_1, HTTPMethod.DELETE);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res); // require user to be logged in
    
    UUID id = null;
    
    try {
      id = UUID.fromString(req.params("article"));
    } catch(IllegalArgumentException e) { }
    
    if(id == null || Stentor.getDatabase().getArticleByID(id) == null)
      throw new EndpointException(req, "Article not found.", 404);
    
    Stentor.getDatabase().deleteArticle(id);
    
    res.status(202);
    return new JSONObject()
        .put(Endpoint.STATUS_KEY, "ok")
        .put(Endpoint.INFO_KEY, "Article deleted.");
  }
  
}
