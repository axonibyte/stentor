package com.axonibyte.stentor.net.restful;

import java.util.UUID;

import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;

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
