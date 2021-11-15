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

@PrepareForTest({ Stentor.class }) public class ModifyArticleEndpointTest {
  
  private static final String REMOTE_ADDR = "127.0.0.1";
  private static final String METHOD = "PATCH";
  private static final String ROUTE = "/v1/articles/:article";
  private static final String CHARSET = "UTF-8";
  private static final String CONTENT_TYPE = "application/json";
  
  private final Endpoint endpoint = new ModifyArticleEndpoint();
  
  @ObjectFactory public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }
  
  @Test public void testDoEndpointTask_malformedBody() throws Exception {
    final String id = UUID.randomUUID().toString();
    final String path = ROUTE.replace(":article", id);
    final ServerInputStringStream reqBody = new ServerInputStringStream("} EAT THE RICH }");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
  }
  
  @Test public void testDoEndpointTask_malformedID() throws Exception {
    final String id = "BAD_ID";
    final String path = ROUTE.replace(":article", id);
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
  }
  
  @Test public void testDoEndpointTask_successNoMods() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    final ServerInputStringStream reqBody = new ServerInputStringStream("{ }");
    
    final String originalTitle = "ORIGINAL TITLE";
    final String originalContent = "ORIGINAL CONTENT";
    final UUID originalAuthor = UUID.randomUUID();
    final long originalTimestamp = System.currentTimeMillis();
    
    final Article article = new Article()
        .setID(id)
        .setAuthor(originalAuthor)
        .setTitle(originalTitle)
        .setContent(originalContent)
        .setTimestamp(originalTimestamp);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getTitle(), originalTitle);
    Assert.assertEquals(article.getContent(), originalContent);
    Assert.assertEquals(article.getAuthor(), originalAuthor);
    Assert.assertEquals(article.getTimestamp(), originalTimestamp);
  }
  
  @Test public void testDoEndpointTask_successTitleMod() throws Exception{
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    
    final String originalTitle = "ORIGINAL TITLE";
    final String modifiedTitle = "MODIFIED TITLE";
    final String originalContent = "ORIGINAL CONTENT";
    final UUID originalAuthor = UUID.randomUUID();
    final long originalTimestamp = System.currentTimeMillis();
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(Article.TITLE_KEY, modifiedTitle)
            .toString());
    
    final Article article = new Article()
        .setID(id)
        .setAuthor(originalAuthor)
        .setTitle(originalTitle)
        .setContent(originalContent)
        .setTimestamp(originalTimestamp);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getTitle(), modifiedTitle);
    Assert.assertEquals(article.getContent(), originalContent);
    Assert.assertEquals(article.getAuthor(), originalAuthor);
    Assert.assertEquals(article.getTimestamp(), originalTimestamp);
  }
  
  @Test public void testDoEndpointTask_successContentMod() throws Exception {
    final UUID id = UUID.randomUUID();
    final String path = ROUTE.replace(":article", id.toString());
    
    final String originalTitle = "ORIGINAL TITLE";
    final String originalContent = "ORIGINAL CONTENT";
    final String modifiedContent = "MODIFIED CONTENT";
    final UUID originalAuthor = UUID.randomUUID();
    final long originalTimestamp = System.currentTimeMillis();
    
    final ServerInputStringStream reqBody = new ServerInputStringStream(
        new JSONObject()
            .put(Article.CONTENT_KEY, modifiedContent)
            .toString());
    
    final Article article = new Article()
        .setID(id)
        .setAuthor(originalAuthor)
        .setTitle(originalTitle)
        .setContent(originalContent)
        .setTimestamp(originalTimestamp);
    
    final Database database = EasyMock.createMock(Database.class);
    EasyMock.expect(database.getArticleByID(id)).andReturn(article).once();
    database.setArticle(article);
    EasyMock.expectLastCall().andAnswer(new EmptyAnswer()).once();
    EasyMock.replay(database);
    
    PowerMock.mockStatic(Stentor.class);
    EasyMock.expect(Stentor.getDatabase()).andReturn(database).times(2);
    PowerMock.replay(Stentor.class);
    
    final HttpServletRequest servletReq = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(servletReq.getContentType()).andReturn(CONTENT_TYPE).once();
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
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getTitle(), originalTitle);
    Assert.assertEquals(article.getContent(), modifiedContent);
    Assert.assertEquals(article.getAuthor(), originalAuthor);
    Assert.assertEquals(article.getTimestamp(), originalTimestamp);
  }
  
}
