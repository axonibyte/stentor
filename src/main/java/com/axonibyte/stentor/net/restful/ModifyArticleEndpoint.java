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
import com.axonibyte.stentor.persistent.Article;

import spark.Request;
import spark.Response;

/**
 * Endpoint to handle article modifications.
 * 
 * @author Caleb L. Power
 */
public class ModifyArticleEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public ModifyArticleEndpoint() {
    super("/articles/:article", APIVersion.VERSION_1, HTTPMethod.PATCH);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res); // require user to be logged in
    
    try {
      UUID id = null;
      
      try {
        id = UUID.fromString(req.params("article"));
      } catch(IllegalArgumentException e) { }
      
      Article article = null;
      article = id == null ? null : Stentor.getDatabase().getArticleByID(id);
      if(article == null) throw new EndpointException(req, "Article not found.", 404);
      
      JSONObject request = new JSONObject(req.body());
      String title = request.has(Article.TITLE_KEY) ? request.getString(Article.TITLE_KEY) : article.getTitle();
      String content = request.has(Article.CONTENT_KEY) ? request.getString(Article.CONTENT_KEY) : article.getContent();
      
      article.setContent(content).setTitle(title);
      Stentor.getDatabase().setArticle(article);
      
      res.status(202);
      return new JSONObject()
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "User updated.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
