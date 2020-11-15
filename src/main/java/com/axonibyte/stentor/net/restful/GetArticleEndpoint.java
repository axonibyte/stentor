package com.axonibyte.stentor.net.restful;

import java.util.UUID;

import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.persistent.Article;

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
    
    return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved article.")
        .put(Article.ID_KEY, article.getID().toString())
        .put(Article.TITLE_KEY, article.getTitle())
        .put(Article.CONTENT_KEY, article.getContent())
        .put(Article.AUTHOR_KEY, article.getAuthor().toString())
        .put(Article.TIMESTAMP_KEY, article.getTimestamp());
  }
  
}
