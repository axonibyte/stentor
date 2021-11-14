package com.axonibyte.stentor.net.restful.article;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.axonibyte.stentor.EmptyAnswer;
import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.persistent.Article;
import com.axonibyte.stentor.persistent.Database;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

@PrepareForTest({ Stentor.class }) public class ListArticlesEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "GET";
  private static final String ROUTE = "/v1/articles";
  
  private final Endpoint endpoint = new ListArticlesEndpoint();
  private final List<Article> articles = new ArrayList<>();
  private final String shortContent = "This is short content for an article.";
  private final String longContent = "This is content for an article. These are the words "
      + "that I need to add so that the length of the test string surpasses the default "
      + "snippet truncation";
  private final String longContentAddendum = " length. Bah weep granah weep nini bon!";
  
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  @BeforeClass public void setup_populateArticleArr() {
    long timestamp = System.currentTimeMillis() - 1000L;
    for(int i = 1; i <= 20; i++) {
      UUID authorID = UUID.randomUUID();
      UUID articleID = UUID.randomUUID();
      String articleTitle = String.format("Article #%1$d", i);
      String articleContent = i % 2 == 0 ? shortContent : (longContent + longContentAddendum);
      long articleTimestamp = timestamp++;
      Article article = new Article()
          .setID(articleID)
          .setAuthor(authorID)
          .setTitle(articleTitle)
          .setContent(articleContent)
          .setTimestamp(articleTimestamp);
      articles.add(article);
    }
  }
  
  private List<Article> generateArticleMockups(int start, int count) {
    List<Article> mockups = new LinkedList<>();
    for(int i = 0; i < articles.size(); i++) {
      Article mockup = EasyMock.createMock(Article.class);
      if(i >= start && i < start + count) {
        Article article = articles.get(i);
        EasyMock.expect(mockup.getAuthor()).andReturn(article.getAuthor()).once();
        EasyMock.expect(mockup.getID()).andReturn(article.getID()).once();
        EasyMock.expect(mockup.getTitle()).andReturn(article.getTitle()).once();
        EasyMock.expect(mockup.getContent()).andReturn(article.getContent()).once();
        EasyMock.expect(mockup.getTimestamp()).andReturn(article.getTimestamp()).once();
      }
      EasyMock.replay(mockup);
      mockups.add(mockup);
    }
    return mockups;
  }
  
  @Test public void testDoEndpointTask_pageInvalid() {
    final String BAD_PAGE_ARG = "2BADBEEF";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(BAD_PAGE_ARG).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
  }
  
  @Test public void testDoEndpointTask_pageBadRange() {
    final String BAD_PAGE_ARG = "0";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(BAD_PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
  }
  
  @Test public void testDoEndpointTask_limitInvalid() {
    final String BAD_LIMIT_ARG = "FADE2BAD";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(BAD_LIMIT_ARG).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
  }
  
  @Test public void testDoEndpointTask_limitBadRange() {
    final String BAD_LIMIT_ARG = "0";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(BAD_LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);;
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
  }
  
  @Test public void testDoEndpointTask_successDefault() throws EndpointException {
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(0, 10)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 10);
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : shortContent);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
    }
    Assert.assertEquals(resBody.getInt("next"), 2);
  }
  
  @Test public void testDoEndpointTask_smallPageSpecified() throws EndpointException {
    final String PAGE_ARG = "2";
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(10, 10)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 10);
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i + 10);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());;
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : shortContent);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
    }
    Assert.assertFalse(resBody.has("next"));
  }
  
  @Test public void testDoEndpointTask_largePageSpecified() throws EndpointException {
    final String PAGE_ARG = "3";
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(20, 0)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer());
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 0);
    Assert.assertFalse(resBody.has("next"));
  }
  
  @Test public void testDoEndpointTask_smallLimitSpecified() throws EndpointException {
    final String LIMIT_ARG = "4";
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(0, 4)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 4);
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : shortContent);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
    }
    Assert.assertEquals(resBody.getInt("next"), 2);
  }
  
  @Test public void testDoEndpointTask_largeLimitSpecified() throws EndpointException {
    final String LIMIT_ARG = "30";
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(0, 20)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 20);
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : shortContent);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
    }
    Assert.assertFalse(resBody.has("next"));
  }
  
  // TODO test success with page and small limit
  @Test public void testDoEndpointTask_pageAndLimitSpecified() throws EndpointException {
    final String PAGE_ARG = "3";
    final String LIMIT_ARG = "4";
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticles()).andReturn(generateArticleMockups(8, 4)).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), 4);
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i + 8);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : shortContent);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
    }
    Assert.assertEquals(resBody.getInt("next"), 4);
  }
  
  // TODO test success with small snippet
  @Test public void testDoEndpointTask_smallSnippetSpecified() {
    
  }
  
  // TODO test success with large snippet
  @Test public void testDoEndpointTask_largeSnippetSpecified() {
    
  }
  
}
