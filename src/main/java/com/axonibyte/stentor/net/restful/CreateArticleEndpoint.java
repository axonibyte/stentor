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
      
      UUID uuid = null;
      do uuid = UUID.randomUUID();
      while(Stentor.getDatabase().getArticleByID(uuid) != null);
      
      Stentor.getDatabase().setArticle(new Article()
          .setAuthor(authToken.getUser().getID())
          .setTitle(title)
          .setContent(content)
          .setID(uuid)
          .setTimestamp(System.currentTimeMillis()));
      
      res.status(201);
      return new JSONObject()
          .put(Endpoint.STATUS_KEY, "ok")
          .put(Endpoint.INFO_KEY, "Article created.");
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error: " + e.getMessage(), 400, e);
    }
  }
  
}
