/*
 * Copyright (c) 2020 Axonibyte Innovations, LLC. All rights reserved.
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
package com.axonibyte.stentor.persistent;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.bson.Document;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.mongodb.client.MongoClient;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * Tests {@link Database} (MongoDB connector wrapper) and ensures that it can
 * appropriately read and write from the datastore.
 * 
 * @author Caleb L. Power
 */
public class DatabaseTest {
  
  private Database database = null;
  private MongoServer server = null;
  
  /**
   * Spins up the mock MongoDB instance.
   */
  @BeforeSuite public void setupMongoDBMockServer() {
    server = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = server.bind();
    database = new Database("mongodb",
        serverAddress.getHostName(),
        serverAddress.getPort(),
        null,
        null,
        "testdb",
        false);
  }
  
  /**
   * Tests {@link Database#getArticleByID(UUID)} to ensure that it handles
   * queries for nonexistent articles appropriately.
   */
  @Test public void testGetArticleByID_missing() {
    final UUID id = new UUID(0, 0);
    Article article = database.getArticleByID(id);
    Assert.assertNull(article);
  }
  
  /**
   * Tests {@link Database#getArticleByID(UUID)} to ensure that it handles
   * queries for existing articles appropriately.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known"
   *         article into the database through the client
   */
  @Test public void testGetArticleByID_found() throws Exception {
    final UUID id = new UUID(-1, 1);
    final UUID author = new UUID(-100, 100);
    final String title = "TITLE";
    final String content = "This is content.";
    final long timestamp = System.currentTimeMillis();
    
    Document doc = new Document(Article.ID_KEY, id.toString())
        .append(Article.TITLE_KEY, title)
        .append(Article.CONTENT_KEY, content)
        .append(Article.AUTHOR_KEY, author.toString())
        .append(Article.TIMESTAMP_KEY, timestamp);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_ARTICLE)
        .insertOne(doc);
    
    Article article = database.getArticleByID(id);
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getAuthor(), author);
    Assert.assertEquals(article.getTitle(), title);
    Assert.assertEquals(article.getContent(), content);
    Assert.assertEquals(article.getTimestamp(), timestamp);
  }
  
  /**
   * Tests {@link Database#setArticle(Article)} to ensure that it can properly
   * add a new article to the database.
   */
  @Test public void testSetArticle_insert() {
    final UUID id = new UUID(-3, 3);
    final UUID author = new UUID(-102, 102);
    final String title = "TITLE";
    final String content = "This is content.";
    final long timestamp = System.currentTimeMillis();
    
    {
      Article article = EasyMock.mock(Article.class);
      EasyMock.expect(article.getID()).andReturn(id).once();
      EasyMock.expect(article.getTitle()).andReturn(title).once();
      EasyMock.expect(article.getContent()).andReturn(content).once();
      EasyMock.expect(article.getTimestamp()).andReturn(timestamp).once();
      EasyMock.expect(article.getAuthor()).andReturn(author).once();
      EasyMock.replay(article);
      
      database.setArticle(article);
      EasyMock.verify(article);
    }
    
    Article article = database.getArticleByID(id);
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getAuthor(), author);
    Assert.assertEquals(article.getTitle(), title);
    Assert.assertEquals(article.getContent(), content);
    Assert.assertEquals(article.getTimestamp(), timestamp);
  }
  
  /**
   * Tests {@link Database#setArticle(Article)} to ensure that it can properly
   * update an existing article.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known"
   *         article into the database through the client
   */
  @Test public void testSetArticle_update() throws Exception {
    final UUID id = new UUID(-2, 2);
    final UUID author1 = new UUID(-101, 101);
    final UUID author2 = new UUID(-201, 201);
    final String title1 = "TITLE";
    final String title2 = "ANOTHER TITLE";
    final String content1 = "This is content.";
    final String content2 = "This is more content.";
    final long timestamp1 = System.currentTimeMillis();
    final long timestamp2 = timestamp1 + 1000L;
    
    Document doc = new Document(Article.ID_KEY, id.toString())
        .append(Article.TITLE_KEY, title1)
        .append(Article.CONTENT_KEY, content1)
        .append(Article.AUTHOR_KEY, author1.toString())
        .append(Article.TIMESTAMP_KEY, timestamp1);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_ARTICLE)
        .insertOne(doc);
    
    {
      Article article = EasyMock.mock(Article.class);
      EasyMock.expect(article.getID()).andReturn(id).once();
      EasyMock.expect(article.getTitle()).andReturn(title2).once();
      EasyMock.expect(article.getContent()).andReturn(content2).once();
      EasyMock.expect(article.getTimestamp()).andReturn(timestamp2).once();
      EasyMock.expect(article.getAuthor()).andReturn(author2).once();
      EasyMock.replay(article);
      
      database.setArticle(article);
      EasyMock.verify(article);
    }
    
    Article article = database.getArticleByID(id);
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(article.getAuthor(), author2);
    Assert.assertEquals(article.getTitle(), title2);
    Assert.assertEquals(article.getContent(), content2);
    Assert.assertEquals(article.getTimestamp(), timestamp2);
  }
  
  /**
   * Tests {@link Database#deleteArticle(UUID)} to ensure that it can properly
   * delete an article.
   */
  @Test public void testDeleteArticle() {
    final UUID id = new UUID(-3, 3);
    final UUID author = new UUID(-102, 102);
    final String title = "TITLE";
    final String content = "This is content.";
    final long timestamp = System.currentTimeMillis();
    
    Article article = EasyMock.mock(Article.class);
    EasyMock.expect(article.getID()).andReturn(id).once();
    EasyMock.expect(article.getTitle()).andReturn(title).once();
    EasyMock.expect(article.getContent()).andReturn(content).once();
    EasyMock.expect(article.getTimestamp()).andReturn(timestamp).once();
    EasyMock.expect(article.getAuthor()).andReturn(author).once();
    EasyMock.replay(article);
      
    database.setArticle(article);
    Assert.assertNotNull(database.getArticleByID(id));
    database.deleteArticle(id);
    Assert.assertNull(database.getArticleByID(id));
    
    EasyMock.verify(article);
  }
  
  /**
   * Tests {@link Database#getUserProfileByID(UUID)} to ensure that it handles
   * queries for nonexistent users appropriately.
   */
  @Test public void testGetUserProfileByID_missing() {
    final UUID id = new UUID(20, -20);
    User user = database.getUserProfileByID(id);
    Assert.assertNull(user);
  }
  
  /**
   * Tests {@link Database#getUserProfileByID(UUID)} to ensure that it handles
   * queries for existing users appropriately.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known"
   *         user into the database through the client
   */
  @Test public void testGetUserProfileByID_found() throws Exception {
    final UUID id = new UUID(22, -22);
    final String username = "luigi";
    final String email = "you@email.com";
    final String phash = "fak3Ha$h";
    
    Document doc = new Document(User.ID_KEY, id.toString())
        .append(User.EMAIL_KEY, email)
        .append(User.USERNAME_KEY, username)
        .append(User.PHASH_KEY, phash);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_USER)
        .insertOne(doc);
    
    User user = database.getUserProfileByID(id);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(user.getEmail(), email);
    Assert.assertEquals(user.getUsername(), username);
    Assert.assertEquals(user.getPasswordHash(), phash);
  }
  
  /**
   * Tests {@link Database#getUserProfileByEmail(String)} to ensure it handles
   * queries for nonexistent users appropriately.
   */
  @Test public void testGetUserProfileByEmail_missing() {
    final String email = "me@email.com";
    User user = database.getUserProfileByEmail(email);
    Assert.assertNull(user);
  }
  
  /**
   * Tests {@link Database#getUserProfileByEmail(String)} to ensure it handles
   * queries for existing users appropriately.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known"
   *         user into the database through the client
   */
  @Test public void testGetUserProfileByEmail_found() throws Exception {
    final UUID id = new UUID(23, -23);
    final String username = "wario";
    final String email = "them@email.com";
    final String phash = "fak3Ha$h";
    
    Document doc = new Document(User.ID_KEY, id.toString())
        .append(User.EMAIL_KEY, email)
        .append(User.USERNAME_KEY, username)
        .append(User.PHASH_KEY, phash);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_USER)
        .insertOne(doc);
    
    User user = database.getUserProfileByEmail(email);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(user.getEmail(), email);
    Assert.assertEquals(user.getUsername(), username);
    Assert.assertEquals(user.getPasswordHash(), phash);
  }
  
  /**
   * Tests {@link Database#getUserProfileByUsername(String)} to ensure that it
   * can handle queries for nonexistent users appropriately.
   */
  @Test public void testGetUserProfileByUsername_missing() {
    final String username = "mario";
    User user = database.getUserProfileByUsername(username);
    Assert.assertNull(user);
  }
  
  /**
   * Tests {@link Database#getUserProfileByUsername(String)} to ensure that it
   * can handle queries for existing users appropriately.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known"
   *         user into the database through the client
   */
  @Test public void testGetUserProfileByUsername_found() throws Exception {
    final UUID id = new UUID(22, -22);
    final String username = "waluigi";
    final String email = "us@email.com";
    final String phash = "fak3Ha$h";
    
    Document doc = new Document(User.ID_KEY, id.toString())
        .append(User.EMAIL_KEY, email)
        .append(User.USERNAME_KEY, username)
        .append(User.PHASH_KEY, phash);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_USER)
        .insertOne(doc);
    
    User user = database.getUserProfileByUsername(username);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(user.getEmail(), email);
    Assert.assertEquals(user.getUsername(), username);
    Assert.assertEquals(user.getPasswordHash(), phash);
  }
  
  /**
   * Tests {@link Database#setUserProfile(User)} to ensure that it can properly
   * add a new user to the database.
   */
  @Test public void testSetUser_insert() {
    final UUID id = new UUID(23, -23);
    final String username = "alice";
    final String email = "alice@email.tld";
    final String phash = "fak3Ha$h";
    
    {
      User user = EasyMock.mock(User.class);
      EasyMock.expect(user.getID()).andReturn(id).once();
      EasyMock.expect(user.getEmail()).andReturn(email).once();
      EasyMock.expect(user.getUsername()).andReturn(username).once();
      EasyMock.expect(user.getPasswordHash()).andReturn(phash).once();
      EasyMock.replay(user);
      
      database.setUserProfile(user);
      EasyMock.verify(user);
    }
    
    User user = database.getUserProfileByID(id);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(user.getEmail(), email);
    Assert.assertEquals(user.getUsername(), username);
    Assert.assertEquals(user.getPasswordHash(), phash);
  }
  
  /**
   * Tests {@link Database#setUserProfile(User)} to ensure that it can properly
   * update an existing user.
   * 
   * @throws Exception iff an exception is thrown when injecting a "known" user
   *         into the database through the client
   */
  @Test public void testSetUser_update() throws Exception {
    final UUID id = new UUID(24, -24);
    final String username1 = "bob";
    final String username2 = "bobby";
    final String email1 = "bob@email.tld";
    final String email2 = "bobby@email.tld";
    final String phash1 = "fak3Ha$h";
    final String phash2 = "N3wF4keH@$h_g3trektskrub";
    
    Document doc = new Document(User.ID_KEY, id.toString())
        .append(User.EMAIL_KEY, email1)
        .append(User.USERNAME_KEY, username1)
        .append(User.PHASH_KEY, phash1);
    
    Field client = database.getClass().getDeclaredField("mongoClient");
    client.setAccessible(true);
    ((MongoClient)client.get(database))
        .getDatabase("testdb")
        .getCollection(Database.COLLECTION_USER)
        .insertOne(doc);
    
    {
      User user = EasyMock.mock(User.class);
      EasyMock.expect(user.getID()).andReturn(id).once();
      EasyMock.expect(user.getEmail()).andReturn(email2).once();
      EasyMock.expect(user.getUsername()).andReturn(username2).once();
      EasyMock.expect(user.getPasswordHash()).andReturn(phash2).once();
      EasyMock.replay(user);
      
      database.setUserProfile(user);
      EasyMock.verify(user);
    }
    
    User user = database.getUserProfileByID(id);
    Assert.assertEquals(user.getID(), id);
    Assert.assertEquals(user.getEmail(), email2);
    Assert.assertEquals(user.getUsername(), username2);
    Assert.assertEquals(user.getPasswordHash(), phash2);
  }
  
  /**
   * Tests {@link Database#deleteArticle(UUID)} to ensure that it can properly
   * delete a user.
   */
  @Test public void testDeleteUser() {
    final UUID id = new UUID(13, 37);
    final String username = "charlie";
    final String email = "charlie@email.tld";
    final String phash = "BEES?";
    
    User user = EasyMock.mock(User.class);
    EasyMock.expect(user.getID()).andReturn(id).once();
    EasyMock.expect(user.getUsername()).andReturn(username).once();
    EasyMock.expect(user.getEmail()).andReturn(email).once();
    EasyMock.expect(user.getPasswordHash()).andReturn(phash).once();
    EasyMock.replay(user);
    
    database.setUserProfile(user);
    Assert.assertNotNull(database.getUserProfileByID(id));
    database.deleteUserProfile(id);
    Assert.assertNull(database.getUserProfileByID(id));
    
    EasyMock.verify(user);
  }
  
  /**
   * Tears down the mock MongoDB instance.
   */
  @AfterSuite public void teardownMongoDBMockServer() {
    server.shutdown();
  }
  
}
