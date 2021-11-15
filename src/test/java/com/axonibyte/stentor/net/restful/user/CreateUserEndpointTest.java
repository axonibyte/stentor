package com.axonibyte.stentor.net.restful.user;

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
import com.axonibyte.stentor.net.ServerInputStringStream;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.persistent.Database;
import com.axonibyte.stentor.persistent.User;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

@PrepareForTest({ Stentor.class }) public class CreateUserEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "POST";
  private static final String ROUTE = "/v1/users";
  private static final String CHARSET = "UTF-8";
  private static final String CONTENT_TYPE = "application/json";
  
  private final Endpoint endpoint = new CreateUserEndpoint();
  
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  @Test public void testDoEndpointTask_malformedBody() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream("} ALL YOUR BASE ARE BELONG TO US {");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
  }
  
  @Test public void testDoEndpointTask_missingEmail() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.USERNAME_KEY, "user")
            .put(User.PASSWORD_KEY, "password")
            .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
      Assert.assertEquals(e.toString(), "Syntax error: JSONObject[\"email\"] not found.");
    }
  }
  
  @Test public void testDoEndpointTask_missingUsername() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, "email")
            .put(User.PASSWORD_KEY, "password")
            .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
      Assert.assertEquals(e.toString(), "Syntax error: JSONObject[\"username\"] not found.");
    }
  }
  
  @Test public void testDoEndpointTask_missingPassword() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, "email")
            .put(User.USERNAME_KEY, "username")
            .toString());
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
      Assert.assertEquals(e.toString(), "Syntax error: JSONObject[\"password\"] not found.");
    }
  }
  
  @Test public void testDoEndpointTask_emailConflict() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, "email")
            .put(User.USERNAME_KEY, "username")
            .put(User.PASSWORD_KEY, "password")
            .toString());
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByEmail("email")).andReturn(new User()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
      Assert.assertEquals(e.getErrorCode(), 409);
      Assert.assertEquals(e.toString(), "Email address conflict.");
    }
  }
  
  @Test public void testDoEndpointTask_usernameConflict() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, "email")
            .put(User.USERNAME_KEY, "username")
            .put(User.PASSWORD_KEY, "password")
            .toString());
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByEmail("email")).andReturn(null).once();
    EasyMock.expect(database.getUserProfileByUsername("username")).andReturn(new User()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
      Assert.assertEquals(e.getErrorCode(), 409);
      Assert.assertEquals(e.toString(), "Username conflict.");
    }
  }
  
  @Test public void testDoEndpointTask_success() throws Exception {
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, "email")
            .put(User.USERNAME_KEY, "username")
            .put(User.PASSWORD_KEY, "password")
            .toString());
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByEmail("email")).andReturn(null).once();
    EasyMock.expect(database.getUserProfileByUsername("username")).andReturn(null).once();
    EasyMock.expect(database.getUserProfileByID(EasyMock.anyObject(UUID.class))).andReturn(null).once();
    database.setUserProfile(EasyMock.anyObject(User.class));
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(4);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
    servletRes.setStatus(201);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.get(Endpoint.INFO_KEY), "User created.");
  }

}
