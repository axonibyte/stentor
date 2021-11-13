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
import com.axonibyte.stentor.persistent.Article;
import com.axonibyte.stentor.persistent.User;

import spark.Request;
import spark.Response;

/**
 * Handles article retrieval.
 * 
 * @author Caleb L. Power
 */
public class GetArticleEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public GetArticleEndpoint() {
    super("/articles/:article", APIVersion.VERSION_1, HTTPMethod.GET);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    Article article = null;
    
    try {
      UUID id = UUID.fromString(req.params("article"));
      if(id != null) article = Stentor.getDatabase().getArticleByID(id);
    } catch(IllegalArgumentException e) { }
    
    if(article == null)
      throw new EndpointException(req, "Article not found.", 404);
    
    User user = Stentor.getDatabase().getUserProfileByID(article.getAuthor());
    
    return new JSONObject()
        .put(Endpoint.STATUS_KEY, "ok")
        .put(Endpoint.INFO_KEY, "Retrieved article.")
        .put(Article.ID_KEY, article.getID().toString())
        .put(Article.TITLE_KEY, article.getTitle())
        .put(Article.CONTENT_KEY, article.getContent())
        .put(Article.AUTHOR_KEY, user == null ? JSONObject.NULL
            : new JSONObject()
                .put(User.ID_KEY, user.getID().toString())
                .put(User.USERNAME_KEY, user.getUsername()))
        .put(Article.TIMESTAMP_KEY, article.getTimestamp());
  }
  
}
