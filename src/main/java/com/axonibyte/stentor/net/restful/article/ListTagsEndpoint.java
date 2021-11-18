package com.axonibyte.stentor.net.restful.article;

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.APIVersion;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.HTTPMethod;
import com.axonibyte.stentor.persistent.Article;

import spark.Request;
import spark.Response;

/**
 * Handles the listing of tags.
 * 
 * @author Caleb L. Power
 */
public class ListTagsEndpoint extends Endpoint {
  
  /**
   * Instantiates the endpoint.
   */
  public ListTagsEndpoint() {
    super("/tags", APIVersion.VERSION_1, HTTPMethod.GET);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public JSONObject doEndpointTask(Request req, Response res, AuthToken authToken) {
    Set<String> tags = new TreeSet<>();
    Stentor.getDatabase().getArticles().forEach(a -> tags.addAll(a.getTags()));
    
    res.status(200);
    return new JSONObject()
        .put(Endpoint.STATUS_KEY, "ok")
        .put(Endpoint.INFO_KEY, "Retrieved tags.")
        .put(Article.TAGS_KEY, tags);
  }
  
}
