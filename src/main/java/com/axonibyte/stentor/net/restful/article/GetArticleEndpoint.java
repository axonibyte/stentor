/*
 * Copyright (c) 2020 Axonibyte Innovations, LLC
 * 
 *   Licensed under the Blue Oak Model License 1.0.0.
 *   See LICENSE for the full terms.
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
    
    res.status(200);
    return new JSONObject()
        .put(Endpoint.STATUS_KEY, "ok")
        .put(Endpoint.INFO_KEY, "Retrieved article.")
        .put(Article.ID_KEY, article.getID().toString())
        .put(Article.TITLE_KEY, article.getTitle())
        .put(Article.CONTENT_KEY, article.getContent())
        .put(Article.TAGS_KEY, article.getTags())
        .put(Article.AUTHOR_KEY, user == null ? JSONObject.NULL
            : new JSONObject()
                .put(User.ID_KEY, user.getID().toString())
                .put(User.USERNAME_KEY, user.getUsername()))
        .put(Article.TIMESTAMP_KEY, article.getTimestamp());
  }
  
}
