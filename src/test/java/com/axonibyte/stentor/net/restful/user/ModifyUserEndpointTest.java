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

@PrepareForTest({ Stentor.class }) public class ModifyUserEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "PATCH";
  private static final String ROUTE = "/v1/users/:user";
  private static final String CHARSET = "UTF-8";
  private static final String CONTENT_TYPE = "application/json";
  
  private final Endpoint endpoint = new ModifyUserEndpoint();
  
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  @Test public void testDoEndpointTask_malformedBody() throws Exception {
    final String id = UUID.randomUUID().toString();
    final String path = ROUTE.replace(":article", id);
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ LOG - BETTER THAN BAD, IT'S GOOD {");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
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
  
  @Test public void testDoEndpointTask_malformedID() throws Exception {
    final String id = "BAD_ID";
    final String path = ROUTE.replace(":user", id);
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
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
      Assert.assertEquals(e.getErrorCode(), 404);
      Assert.assertEquals(e.toString(), "User not found.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  @Test public void testDoEndpointTask_nonexistentUser() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(null).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
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
      Assert.assertEquals(e.getErrorCode(), 404);
      Assert.assertEquals(e.toString(), "User not found.");
    }
    
    EasyMock.verify(database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_badAuthMismatchedUser() throws Exception {
    final UUID aliceID = new UUID(1, 1);
    final UUID bobID = new UUID(3, 5);
    final String path = ROUTE.replace(":user", bobID.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final User alice = EasyMock.createMock(User.class);
    EasyMock.expect(alice.getID()).andReturn(aliceID).once();
    EasyMock.replay(alice);    
    
    final User bob = EasyMock.createMock(User.class);
    EasyMock.replay(bob);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(bobID)).andReturn(bob).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).once();
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(alice).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 403);
      Assert.assertEquals(e.toString(), "Access denied.");
    }
    
    EasyMock.verify(alice, bob, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successNoMods() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_conflictingEmail() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String oldEmail = "old@email.tld";
    final String newEmail = "new@email.tld";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, newEmail)
            .toString());
    
    final User alice = EasyMock.createMock(User.class);
    EasyMock.expect(alice.getID()).andReturn(id).once();
    EasyMock.expect(alice.getEmail()).andReturn(oldEmail).once();
    EasyMock.replay(alice);
    
    final User bob = EasyMock.createMock(User.class);
    EasyMock.replay(bob);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(alice).once();
    EasyMock.expect(database.getUserProfileByEmail(newEmail)).andReturn(bob).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(alice).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 409);
      Assert.assertEquals(e.toString(), "Email address conflict.");
    }
    
    EasyMock.verify(alice, bob, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successIdenticalEmail() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String email = "me@email.tld";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, email)
            .toString());
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.getEmail()).andReturn(email).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successModifiedEmail() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String oldEmail = "old@email.tld";
    final String newEmail = "new@email.tld";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.EMAIL_KEY, newEmail)
            .toString());
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.getEmail()).andReturn(oldEmail).once();
    EasyMock.expect(user.setEmail(newEmail)).andReturn(user).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    EasyMock.expect(database.getUserProfileByEmail(newEmail)).andReturn(null).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(3);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
    
  @Test public void testDoEndpointTask_conflictingUsername() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String oldUsername = "oldIGN";
    final String newUsername = "newIGN";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.USERNAME_KEY, newUsername)
            .toString());
    
    final User alice = EasyMock.createMock(User.class);
    EasyMock.expect(alice.getID()).andReturn(id).once();
    EasyMock.expect(alice.getUsername()).andReturn(oldUsername).once();
    EasyMock.replay(alice);
    
    final User bob = EasyMock.createMock(User.class);
    EasyMock.replay(bob);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(alice).once();
    EasyMock.expect(database.getUserProfileByUsername(newUsername)).andReturn(bob).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getRemoteAddr()).andReturn(REMOTE_ADDR).once();
    EasyMock.expect(servletReq.getMethod()).andReturn(METHOD).once();
    EasyMock.expect(servletReq.getPathInfo()).andReturn(path).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(alice).once();
    EasyMock.replay(authToken);
    
    try {
      endpoint.doEndpointTask(req, res, authToken);
      Assert.fail("An EndpointException was not thrown.");
    } catch(EndpointException e) {
      Assert.assertEquals(e.getErrorCode(), 409);
      Assert.assertEquals(e.toString(), "Username conflict.");
    }
    
    EasyMock.verify(alice, bob, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successIdenticalUsername() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String username = "coolguy";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.USERNAME_KEY, username)
            .toString());
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.getUsername()).andReturn(username).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successModifiedUsername() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String oldUsername = "oldIGN";
    final String newUsername = "newIGN";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.USERNAME_KEY, newUsername)
            .toString());
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.getUsername()).andReturn(oldUsername).once();
    EasyMock.expect(user.setUsername(newUsername)).andReturn(user).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    EasyMock.expect(database.getUserProfileByUsername(newUsername)).andReturn(null).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(3);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successModifiedPassword() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":user", id.toString());
    final String password = "password";
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(User.PASSWORD_KEY, password)
            .toString());
    
    final User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.setPassword(password)).andReturn(user).once();
    EasyMock.replay(user);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getUserProfileByID(id)).andReturn(user).once();
    database.setUserProfile(user);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getCharacterEncoding()).andReturn(CHARSET).once();
    EasyMock.expect(servletReq.getInputStream()).andReturn(reqBody).once();
    EasyMock.replay(servletReq);
    Request req = RequestResponseFactory.create(
        new RouteMatch(null, ROUTE, path, null),
        servletReq);
    
    final HttpServletResponse servletRes = EasyMock.createMock(HttpServletResponse.class);
    servletRes.setStatus(202);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(servletRes);
    Response res = RequestResponseFactory.create(servletRes);
    
    final AuthToken authToken = EasyMock.createMock(AuthToken.class);
    EasyMock.expect(authToken.hasClientPerms()).andReturn(true).once();
    EasyMock.expect(authToken.getUser()).andReturn(user).once();
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "User updated.");
    
    EasyMock.verify(user, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
}
