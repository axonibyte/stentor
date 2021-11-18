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
import org.json.JSONArray;
import org.json.JSONObject;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.axonibyte.stentor.EmptyAnswer;
import com.axonibyte.stentor.Stentor;
import com.axonibyte.stentor.net.ServerInputStringStream;
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
 * Test class to test {@link CreateArticleEndpoint}.
 * 
 * @author Caleb L. Power
 */
@PrepareForTest({ Stentor.class }) public final class CreateArticleEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "POST";
  private static final String ROUTE = "/v1/articles";
  private static final String CHARSET = "UTF-8";
  
  private final Endpoint endpoint = new CreateArticleEndpoint();
  
  /**
   * Retrieves the PowerMock object factory for TestNG.
   * 
   * @return an instance of PowerMock's object factory
   */
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  /**
   * Tests {@link CreateArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when a malformed request body is sent.
   * 
   * @throws Exception iff any exception other than {@link EndpointException}
   *         is thrown during the execution of the test method
   */
  @Test public void testDoEndpointTask_malformedBody() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream("} BAD WOLF {");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, ROUTE, null),
        servletReq);
    
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
      Assert.assertTrue(e.toString().startsWith("Syntax error: "));
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link CreateArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when the title is not present in the
   * body of the request.
   * 
   * @throws Exception iff any exception other than {@link EndpointException}
   *         is thrown during the execution of the test method
   */
  @Test public void testDoEndpointTask_missingTitle() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
          .put("content", "Here is some content.")
          .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, ROUTE, null),
        servletReq);
    
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
      Assert.assertTrue(e.toString().startsWith("Syntax error: "));
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link CreateArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it fails gracefully when content is not present in the body
   * of the request.
   * 
   * @throws Exception iff any exception other than {@link EndpointException}
   *         is thrown during the execution of the test.
   */
  @Test public void testDoEndpointTask_missingContent() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
          .put("title", "Here is a title.")
          .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(ROUTE).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, ROUTE, null),
        servletReq);
    
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
      Assert.assertTrue(e.toString().startsWith("Syntax error: "));
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  /**
   * Tests {@link CreateArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it behaves appropriately when it is provided with input
   * that is well-formed and otherwise valid (without tags, which are optional).
   * 
   * @throws Exception iff any exception is thrown during execution.
   */
  @Test public void testDoEndpointTask_successNoTags() throws Exception {
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(EasyMock.anyObject(UUID.class))).andReturn(null).once();
    database.setArticle(EasyMock.anyObject(Article.class));
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
          .put("title", "Here is a title.")
          .put("content", "Here is some content.")
          .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, ROUTE, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(201);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(new UUID(0L, 0L)).once();
    EasyMock.replay(user);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Article created.");
    
    EasyMock.verify(database, servletReq, servletRes, user, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  /**
   * Tests {@link CreateArticleEndpoint#doEndpointTask(Request, Response, AuthToken)}
   * to ensure that it behaves appropriately when it is provided with input
   * that is well-formed and otherwise valid (with tags, which are optional).
   * 
   * @throws Exception iff any exception is thrown during execution.
   */
  @Test public void testDoEndpointTask_successWithTags() throws Exception {
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(EasyMock.anyObject(UUID.class))).andReturn(null).once();
    database.setArticle(EasyMock.anyObject(Article.class));
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
          .put("title", "Here is a title.")
          .put("content", "Here is some content.")
          .put("tags", new JSONArray()
              .put("alpha")
              .put("beta")
              .put("gamma"))
          .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody);
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, ROUTE, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(201);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(new UUID(0L, 0L)).once();
    EasyMock.replay(user);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Article created.");
    
    EasyMock.verify(database, servletReq, servletRes, user, authToken);
    PowerMock.verify(Stentor.class);
  }
  
}
