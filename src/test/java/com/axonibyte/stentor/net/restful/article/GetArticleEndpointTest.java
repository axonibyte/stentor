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

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.json.JSONObject;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.axonibyte.stentor.EmptyAnswer;
import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.persistent.Article;
import com.axonibyte.stentor.persistent.Database;
import com.axonibyte.stentor.persistent.User;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

/**
 * Test class to test {@link GetArticleEndpoint}.
 * 
 * @author Caleb L. Power
 */
@PrepareForTest({ Stentor.class }) public final class GetArticleEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "GET";
  private static final String ROUTE = "/v1/articles/:article";
  
  private final Endpoint endpoint = new GetArticleEndpoint();
  
  /**
   * Retrieves the PowerMock object factory for TestNG.
   * 
   * @return an instance of PowerMock's object factory
   */
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  /**
   * Tests {@link GetArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a malformed article ID is passed
   * as a URL argument.
   */
  @Test public void testDoEndpointTask_badID() {
    final String id = "BAD_ID";
    final String path = ROUTE.replace(":article", id);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 404);
      Assert.assertEquals(e.toString(), "Article not found.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link GetArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when the provided well-formed article
   * ID does not match a known article in the database.
   */
  @Test public void testDoEndpointTask_nonexistentArticle() {
    final UUID id = new UUID(0, 0);
    final String path = ROUTE.replace(":article", id.toString());
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(null).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 404);
      Assert.assertEquals(e.toString(), "Article not found.");
    }
    
    EasyMock.verify(database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  /**
   * Tests {@link GetArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully retrieves the requested article, even if
   * the article's author cannot be found or is not specified.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_successNoAuthor() throws EndpointException {
    final UUID articleID = new UUID(0, 0);
    final UUID userID = new UUID(-1L, 1L);
    final String path = ROUTE.replace(":article", articleID.toString());
    final String articleTitle = "ARTICLE_TITLE";
    final String articleContent = "ARTICLE_CONTENT";
    final long articleTimestamp = 1L;
    
    final Article article = EasyMock.createMock(Article.class);
    EasyMock.expect(article.getAuthor()).andReturn(userID).once();
    EasyMock.expect(article.getID()).andReturn(articleID).once();
    EasyMock.expect(article.getTitle()).andReturn(articleTitle).once();
    EasyMock.expect(article.getContent()).andReturn(articleContent).once();
    EasyMock.expect(article.getTimestamp()).andReturn(articleTimestamp).once();
    EasyMock.replay(article);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(articleID)).andReturn(article).once();
    EasyMock.expect(database.getUserProfileByID(userID)).andReturn(null).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved article.");
    Assert.assertEquals(resBody.getString(Article.ID_KEY), articleID.toString());
    Assert.assertEquals(resBody.getString(Article.TITLE_KEY), articleTitle);
    Assert.assertEquals(resBody.getString(Article.CONTENT_KEY), articleContent);
    Assert.assertEquals(resBody.getLong(Article.TIMESTAMP_KEY), articleTimestamp);
    Assert.assertTrue(resBody.isNull(articleContent));
    
    EasyMock.verify(article, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  /**
   * Tests {@link GetArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it successfully retrieves the requested article and some
   * of the author's metadata when the article exists and its author is known.
   * 
   * @throws EndpointException iff an {@link EndpointException} is thrown
   *         during the execution of this test method
   */
  @Test public void testDoEndpointTask_successWithAuthor() throws EndpointException {
    final UUID articleID = new UUID(0, 0);
    final UUID userID = new UUID(-1L, 1L);
    final String path = ROUTE.replace(":article", articleID.toString());
    final String articleTitle = "ARTICLE TITLE";
    final String articleContent = "ARTICLE CONTENT";
    final String userName = "BOB THE BUILDER";
    final long articleTimestamp = 1L;
    
    final Article article = EasyMock.createMock(Article.class);
    EasyMock.expect(article.getAuthor()).andReturn(userID).once();
    EasyMock.expect(article.getID()).andReturn(articleID).once();
    EasyMock.expect(article.getTitle()).andReturn(articleTitle).once();
    EasyMock.expect(article.getContent()).andReturn(articleContent).once();
    EasyMock.expect(article.getTimestamp()).andReturn(articleTimestamp).once();
    EasyMock.replay(article);
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(userID).once();
    EasyMock.expect(user.getUsername()).andReturn(userName).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(articleID)).andReturn(article).once();
    EasyMock.expect(database.getUserProfileByID(userID)).andReturn(user).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(200);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Retrieved article.");
    Assert.assertEquals(resBody.getString(Article.ID_KEY), articleID.toString());
    Assert.assertEquals(resBody.getString(Article.TITLE_KEY), articleTitle);
    Assert.assertEquals(resBody.getString(Article.CONTENT_KEY), articleContent);
    Assert.assertEquals(resBody.getLong(Article.TIMESTAMP_KEY), articleTimestamp);
    Assert.assertEquals(resBody.getJSONObject(Article.AUTHOR_KEY).getString(User.ID_KEY), userID.toString());
    Assert.assertEquals(resBody.getJSONObject(Article.AUTHOR_KEY).getString(User.USERNAME_KEY), userName);
    
    EasyMock.verify(article, user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
}
