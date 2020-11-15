/*
 * Copyright (c) 2020 V2C Development Team. All rights reserved.
 * Licensed under the Version 0.0.1 of the V2C License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at <https://tinyurl.com/v2c-license>.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions 
 * limitations under the License.
 */
package com.axonibyte.stentor.persistent;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * A driver to interact with MongoDB.
 * 
 * @author Caleb L. Power
 */
public class Database {
  
  private static String DB_NAME = "v2cDashboard";
  private static String COLLECTION_ARTICLE = "article";
  private static String COLLECTION_USER = "user";
  
  private MongoClient mongoClient = null;
  
  /**
   * Instantiates the database.
   * 
   * @param connection the host and port of the MongoDB server
   */
  public Database(String connection) {
    this.mongoClient = MongoClients.create("mongodb://" + connection);
  }
  
  /**
   * Retrieves a particular article by ID if it exists.
   * 
   * @param id the unique identifier of the article
   * @return the resulting author, or {@code null} if no such article exists
   */
  public Article getArticleByID(UUID id) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_ARTICLE);
    Document document = collection.find(Filters.eq(Article.ID_KEY, id.toString())).first();
    if(document != null) return new Article()
        .setTitle(document.getString(Article.TITLE_KEY))
        .setContent(document.getString(Article.CONTENT_KEY))
        .setTimestamp(document.getLong(Article.TIMESTAMP_KEY))
        .setAuthor(UUID.fromString(document.getString(Article.AUTHOR_KEY)))
        .setID(UUID.fromString(document.getString(Article.ID_KEY)));
    return null;
  }
  
  /**
   * Retrieves all articles, in descending order by timestamp.
   * 
   * @return a list of articles in descending order by timestamp
   */
  public List<Article> getArticles() {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_ARTICLE);
    FindIterable<Document> documents = collection.find().sort(new BasicDBObject(Article.TIMESTAMP_KEY, -1));
    List<Article> articles = new LinkedList<>();
    for(Document document : documents)
      articles.add(new Article()
          .setTitle(document.getString(Article.TITLE_KEY))
          .setContent(document.getString(Article.CONTENT_KEY))
          .setTimestamp(document.getLong(Article.TIMESTAMP_KEY))
          .setAuthor(UUID.fromString(document.getString(Article.AUTHOR_KEY)))
          .setID(UUID.fromString(document.getString(Article.ID_KEY))));
    return articles;
  }
  
  /**
   * Replaces an article, or creates oneif it does not already exist.
   * 
   * @param article the new article
   */
  public void setArticle(Article article) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_ARTICLE);
    String id = article.getID().toString();
    Document document = new Document(Article.ID_KEY, id)
        .append(Article.TITLE_KEY, article.getTitle())
        .append(Article.CONTENT_KEY, article.getContent())
        .append(Article.TIMESTAMP_KEY, article.getTimestamp())
        .append(Article.AUTHOR_KEY, article.getAuthor().toString());
    if(collection.find(Filters.eq(Article.ID_KEY, id)).first() == null)
      collection.insertOne(document);
    else collection.replaceOne(Filters.eq(Article.ID_KEY, id), document);
  }
  
  /**
   * Deletes an article from the database.
   * 
   * @param article the article's unique identifier
   */
  public void deleteArticle(UUID article) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_ARTICLE);
    collection.deleteMany(Filters.eq(Article.ID_KEY, article.toString()));
  }
  
  /**
   * Retrieves a particular user's profile by ID if it exists.
   * 
   * @param id the unique identifier of the user
   * @return the resulting user, or {@code null} if no such user exists
   */
  public User getUserProfileByID(UUID id) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq(User.ID_KEY, id.toString())).first();
    if(document != null) return new User()
        .setEmail(document.getString(User.EMAIL_KEY))
        .setUsername(document.getString(User.USERNAME_KEY))
        .setPasswordHash(document.getString(User.PHASH_KEY))
        .setID(UUID.fromString(document.getString(User.ID_KEY)));
    return null;
  }
  
  /**
   * Retrieves a particular user's profile by ID if it exists.
   * 
   * @param email the user's email
   * @return the resulting user, or <code>null</code> if no such user exists
   */
  public User getUserProfileByEmail(String email) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq(User.EMAIL_KEY, email)).first();
    if(document != null) return new User()
        .setEmail(document.getString(User.EMAIL_KEY))
        .setUsername(document.getString(User.USERNAME_KEY))
        .setPasswordHash(document.getString(User.PHASH_KEY))
        .setID(UUID.fromString(document.getString(User.ID_KEY)));
    return null;
  }
  
  /**
   * Retrieves a particular user's profile by username if it exists
   * 
   * @param username the user's username
   * @return the resulting user, or <code>null</code> if no such user exists
   */
  public User getUserProfileByUsername(String username) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    Document document = collection.find(Filters.eq(User.USERNAME_KEY, username)).first();
    if(document != null) return new User()
        .setEmail(document.getString(User.EMAIL_KEY))
        .setUsername(document.getString(User.USERNAME_KEY))
        .setPasswordHash(document.getString(User.PHASH_KEY))
        .setID(UUID.fromString(document.getString(User.ID_KEY)));
    return null;
  }
  
  /**
   * Replaces a user's profile, or creates one if it does not already exist.
   * 
   * @param user the new user profile
   */
  public void setUserProfile(User user) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    String id = user.getID().toString();
    Document document = new Document(User.ID_KEY, id)
        .append(User.EMAIL_KEY, user.getEmail())
        .append(User.USERNAME_KEY, user.getUsername())
        .append(User.PHASH_KEY, user.getPasswordHash());
    if(collection.find(Filters.eq(User.ID_KEY, id)).first() == null)
      collection.insertOne(document);
    else collection.replaceOne(Filters.eq(User.ID_KEY, id), document);
  }
  
  /**
   * Deletes a user profile from the database.
   * 
   * @param user the unique identifier of the user profile
   */
  public void deleteUserProfile(UUID user) {
    MongoDatabase database = mongoClient.getDatabase(DB_NAME);
    MongoCollection<Document> collection = database.getCollection(COLLECTION_USER);
    collection.deleteMany(Filters.eq(User.ID_KEY, user.toString()));
  }

}
