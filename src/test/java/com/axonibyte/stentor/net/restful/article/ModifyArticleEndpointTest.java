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
import com.axonibyte.stentor.net.ServerInputStringStream;
import com.axonibyte.stentor.net.auth.AuthToken;
import com.axonibyte.stentor.net.restful.Endpoint;
import com.axonibyte.stentor.net.restful.EndpointException;
import com.axonibyte.stentor.persistent.Article;
import com.axonibyte.stentor.persistent.Database;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

@PrepareForTest({ Stentor.class }) public final class ModifyArticleEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "PATCH";
  private static final String ROUTE = "/v1/articles/:article";
  private static final String CHARSET = "UTF-8";
  
  private final Endpoint endpoint = new ModifyArticleEndpoint();
  
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  @Test public void testDoEndpointTask_malformedBody() throws Exception {
    final String id = UUID.randomUUID().toString();
    final String path = ROUTE.replace(":article", id);
    final ServerInputStringStream reqBody = new ServerInputStringStream("} EAT THE RICH }");
    
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
    final String path = ROUTE.replace(":article", id);
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
      Assert.assertEquals(e.toString(), "Article not found.");
    }
    
    EasyMock.verify(servletReq, servletRes, authToken);
  }
  
  @Test public void testDoEndpointTask_nonexistentArticle() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(null).once();
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
      Assert.assertEquals(e.toString(), "Article not found.");
    }
    
    EasyMock.verify(database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successNoMods() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final Article article = EasyMock.createMock(Article.class);
    EasyMock.replay(article);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
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
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Article updated.");
    
    EasyMock.verify(article, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successTitleMod() throws Exception{
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    final String title = "modified title";
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(Article.TITLE_KEY, title)
            .toString());
    
    final Article article = EasyMock.createMock(Article.class);
    EasyMock.expect(article.setTitle(title)).andReturn(article).once();
    EasyMock.replay(article);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
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
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Article updated.");
    
    EasyMock.verify(article, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
  @Test public void testDoEndpointTask_successContentMod() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    final String content = "modified content";
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(Article.CONTENT_KEY, content)
            .toString());
    
    final Article article = EasyMock.createMock(Article.class);
    EasyMock.expect(article.setContent(content)).andReturn(article).once();
    EasyMock.replay(article);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
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
    EasyMock.replay(authToken);
    
    JSONObject resBody = endpoint.doEndpointTask(req, res, authToken);
    Assert.assertEquals(resBody.getString(Endpoint.STATUS_KEY), "ok");
    Assert.assertEquals(resBody.getString(Endpoint.INFO_KEY), "Article updated.");
    
    EasyMock.verify(article, database, servletReq, servletRes, authToken);
    PowerMock.verify(Stentor.class);
  }
  
}
