/*
 * Copyright (c) 2021 Axonibyte Innovations, LLC. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
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

/**
 * Test class to test {@link ListArticlesEndpoint}.
 * 
 * @author Caleb L. Power
 */
@PrepareForTest({ Stentor.class }) public final class ListArticlesEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "GET";
  private static final String ROUTE = "/v1/articles";
  
  private final Endpoint endpoint = new ListArticlesEndpoint();
  private final List<Article> articles = new ArrayList<>();
  private final String[] content = {
      "This is content for an article.", // 31 characters
      " These are the words that I need so that the length of the test", // 63 characters, sum = 94
      " string surpasses the default snippet truncation", // 48 characters, sum = 142
      " length. Bah weep granah weep nini bon!" // 39 characters, sum = 181
  };
  
  /**
   * Retrieves the PowerMock object factory for TestNG.
   * 
   * @return an instance of PowerMock's object factory
   */
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  /**
   * Populates a list articles for use in testing during the execution of
   * various test methods in this class. Executes before said methods.
   */
  @BeforeClass public void setup_populateArticleArr() {
    long timestamp = System.currentTimeMillis() - 1000L;
    for(int i = 1; i <= 20; i++) {
      UUID authorID = UUID.randomUUID();
      UUID articleID = UUID.randomUUID();
      String articleTitle = String.format("Article #%1$d", i);
      String articleContent = content[0] + (i % 2 == 1 ? content[1] + content[2] + content[3] : "");
      long articleTimestamp = timestamp++;
      Set<String> tags = new TreeSet<>();
      if(i % 2 == 0) tags.add("alpha");
      if(i % 3 == 0) tags.add("beta");
      if(i % 5 == 0) tags.add("gamma");
      Article article = new Article()
          .setID(articleID)
          .setAuthor(authorID)
          .setTitle(articleTitle)
          .setContent(articleContent)
          .setTags(tags)
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
        EasyMock.expect(mockup.getTags()).andReturn(article.getTags()).once();
      }
      EasyMock.replay(mockup);
      mockups.add(mockup);
    }
    return mockups;
  }
  
  private void validateSuccessResponse(Database database, HttpServletRequest servletReq, List<Article> generatedArticles, int arrLen, int arrOffset, int maxContent, int next) throws EndpointException, JSONException {
    final Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    StringBuilder longContentBuilder = new StringBuilder(content[0]);
    for(int i = 1; i <= maxContent; i++) longContentBuilder.append(content[i]);
    String longContent = longContentBuilder.toString();
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved articles.");
    JSONArray articleArr = resBody.getJSONArray("articles");
    Assert.assertEquals(articleArr.length(), arrLen);
    
    for(int i = 0; i < articleArr.length(); i++) {
      Article corresponding = articles.get(i + arrOffset);
      JSONObject articleObj = (JSONObject)articleArr.get(i);
      Assert.assertEquals(articleObj.getString(Article.ID_KEY), corresponding.getID().toString());
      Assert.assertEquals(articleObj.getString(Article.TITLE_KEY), corresponding.getTitle());
      Assert.assertEquals(articleObj.getString(Article.CONTENT_KEY), i % 2 == 0 ? longContent : content[0]);
      Assert.assertEquals(articleObj.getString(Article.AUTHOR_KEY), corresponding.getAuthor().toString());
      Assert.assertEquals(articleObj.getLong(Article.TIMESTAMP_KEY), corresponding.getTimestamp());
      Set<String> resTags = new TreeSet<>();
      articleObj.getJSONArray(Article.TAGS_KEY).forEach(o -> resTags.add((String)o));
      Assert.assertEquals(resTags, corresponding.getTags());
    }
    
    if(next > 1)
      Assert.assertEquals(resBody.getInt("next"), next);
    else
      Assert.assertFalse(resBody.has("next"));
    
    generatedArticles.forEach(a -> EasyMock.verify(a));
    EasyMock.verify(database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a malformed page number is passed
   * as a URL query argument.
   */
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
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a bad (non-positive) page range is
   * passed as a URL query argument.
   */
  @Test public void testDoEndpointTask_pageBadRange() {
    final String BAD_PAGE_ARG = "0";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(BAD_PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a malformed limit is passed as a
   * URL query argument.
   */
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
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a bad (non-positive) limit is
   * passed as a URL query argument.
   */
  @Test public void testDoEndpointTask_limitBadRange() {
    final String BAD_LIMIT_ARG = "0";
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(BAD_LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(new RouteMatch(null, ROUTE, ROUTE, null), servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 400);
      Assert.assertEquals(e.toString(), "Syntax error.");
    }
    
    EasyMock.verify(servletRes, servletReq, authToken);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully lists articles using the default parameter
   * values when no query arguments are passed.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_successDefault() throws EndpointException {
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 10);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 10, 0, 2, 2);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully lists articles when a relatively low page
   * number is specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_smallPageSpecified() throws EndpointException {
    final String PAGE_ARG = "2";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(10, 10);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 10, 10, 2, 0);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully returns, but does not list out-of-range
   * articles when a relatively high page number is specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_largePageSpecified() throws EndpointException {
    final String PAGE_ARG = "3";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(20, 0);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 0, 0, 0, 0);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully lists articles when a relatively small
   * limit is specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_smallLimitSpecified() throws EndpointException {
    final String LIMIT_ARG = "4";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 4);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 4, 0, 2, 2);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully lists all articles when a limit greater
   * than the number of items available is specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_largeLimitSpecified() throws EndpointException {
    final String LIMIT_ARG = "30";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 20);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 20, 0, 2, 0);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully list the appropriate range and quantity of
   * articles when both the page and limit are specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_pageAndLimitSpecified() throws EndpointException {
    final String PAGE_ARG = "3";
    final String LIMIT_ARG = "4";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(8, 4);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(PAGE_ARG).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 4, 8, 2, 4);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully truncates the snippet at the appropriate length
   * in such a manner in which it does divide a word in an unreadable fashion.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_smallSnippetSpecified() throws EndpointException {
    final String LIMIT_ARG = "92";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 10);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 10, 0, 1, 2);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that is successfully returns the entirety of an article's
   * content if the specified truncation length is larger than the length of
   * the content itself.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_largeSnippetSpecified() throws EndpointException {
    final String LIMIT_ARG = "280";
    
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 10);
    EasyMock.expect(database.getArticles()).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(LIMIT_ARG).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn(null).once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 10, 0, 3, 2);
  }
  
  /**
   * Tests {@link ListArticlesEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that is successfully lists articles when a tag is specified.
   * Note that in order to decouple the database logic from this unit test,
   * the generated article mockups do not necessarily match the endpoint query
   * arguments.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_successWithTag() throws EndpointException {
    final Database database = EasyMock.createMock(Database.class);
    final var articles = generateArticleMockups(0, 10);
    EasyMock.expect(database.getArticlesByTag("beta")).andReturn(articles).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getParameter("page")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("limit")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("snippet")).andReturn(null).once();
    EasyMock.expect(servletReq.getParameter("tag")).andReturn("beta").once();
    EasyMock.replay(servletReq);
    
    validateSuccessResponse(database, servletReq, articles, 10, 0, 2, 2);
  }
  
}
