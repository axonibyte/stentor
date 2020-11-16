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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.persistent.Article;

import spark.Request;
import spark.Response;

/**
 * Handles the listing of articles.
 * 
 * @author Caleb L. Power
 */
public class ListArticlesEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public ListArticlesEndpoint() {
    super("/articles", APIVersion.VERSION_1, HTTPMethod.GET);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    try {
      int page = Integer.parseInt(req.queryParamOrDefault("page", "1"));
      int limit = Integer.parseInt(req.queryParamOrDefault("limit", "10"));
      
      if(page < 1 || limit < 1)
        throw new EndpointException(req, "Syntax error.", 400);
      
      List<Article> articles = Stentor.getDatabase().getArticles();
      for(int i = (page - 1) * limit; i > 0 && !articles.isEmpty(); i--)
        articles.remove(0);
      
      JSONArray articleArr = new JSONArray();
      
      for(int i = 0; i < limit && !articles.isEmpty(); i++) {
        Article article = articles.remove(0);
        articleArr.put(new JSONObject()
            .put(Article.ID_KEY, article.getID().toString())
            .put(Article.TITLE_KEY, article.getTitle())
            .put(Article.CONTENT_KEY, article.getContent())
            .put(Article.AUTHOR_KEY, article.getAuthor().toString())
            .put(Article.TIMESTAMP_KEY, article.getTimestamp()));
      }
      
      JSONObject response = new JSONObject()
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "Retrieved articles.")
          .put("articles", articleArr);
      if(!articles.isEmpty())
        response.put("next", page + 1);
      
      res.status(200);
      return response;
    } catch(NumberFormatException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    }
  }
  
}
