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

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.net.restful.HTTPMethod;
import com.axonibyte.stentor.persistent.Article;

import spark.Request;
import spark.Response;

/**
 * Endpoint to handle article creation.
 * 
 * @author Caleb L. Power
 */
public class CreateArticleEndpoint extends Endpoint {

  /**
   * Instantiates the endpoint.
   */
  public CreateArticleEndpoint() {
    super("/articles", APIVersion.VERSION_1, HTTPMethod.POST);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) throws EndpointException {
    authorize(authToken, req, res);
    
    try {
      JSONObject request = new JSONObject(req.body());
      String title = request.getString(Article.TITLE_KEY);
      String content = request.getString(Article.CONTENT_KEY);
      JSONArray tagArr = request.optJSONArray(Article.TAGS_KEY);
      
      UUID uuid = null;
      do uuid = UUID.randomUUID();
      while(Stentor.getDatabase().getArticleByID(uuid) != null);
      
      Article article = new Article()
          .setAuthor(authToken.getUser().getID())
          .setTitle(title)
          .setContent(content)
          .setID(uuid)
          .setTimestamp(System.currentTimeMillis());
      
      if(tagArr != null) {
        Set<String> tags = new TreeSet<>();
        for(int i = 0; i < tagArr.length(); i++)
          tags.add(tagArr.getString(i));
        article.setTags(tags);
      }
      
      Stentor.getDatabase().setArticle(article);
      
      res.status(201);
      return new JSONObject()
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "Article created.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
