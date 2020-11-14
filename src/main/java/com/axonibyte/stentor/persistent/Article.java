package com.axonibyte.stentor.persistent;

import java.util.UUID;

/**
 * Represents some article.
 * 
 * @author Caleb L. Power
 */
public class Article {
  
  /**
   * Database key associated with the title.
   */
  public static final String TITLE_KEY = "title";
  
  /**
   * Database key associated with the content.
   */
  public static final String CONTENT_KEY = "content";
  
  /**
   * Database key associated with the timestamp;
   */
  public static final String TIMESTAMP_KEY = "timestamp";
  
  /**
   * Database key associated with the author.
   */
  public static final String AUTHOR_KEY = "author";
  
  /**
   * Database key associated with the article's unique identifier.
   */
  public static final String ID_KEY = "id";
  
  private UUID id = null;
  private UUID author = null;
  private String title = null;
  private String content = null;
  private long timestamp = 0L;
  
  /**
   * Retrieves the article's unique ID.
   * 
   * @return the UUID associated with the article
   */
  public UUID getID() {
    return id;
  }
  
  /**
   * Sets the article's unique ID.
   * 
   * @param id the UUID associated with the article
   * @return this Article
   */
  public Article setID(UUID id) {
    this.id = id;
    return this;
  }
  
  /**
   * Retrieves the UUID of the author of this article.
   * 
   * @return the author's unique identifier
   */
  public UUID getAuthor() {
    return author;
  }
  
  /**
   * Sets the UUID of the author of this article.
   * 
   * @param author the author's unique identifier
   * @return this Article
   */
  public Article setAuthor(UUID author) {
    this.author = author;
    return this;
  }
  
  /**
   * Retrieves the title of this article.
   * 
   * @return the article's title
   */
  public String getTitle() {
    return title;
  }
  
  /**
   * Sets the title of the article.
   * 
   * @param title the article's title
   * @return this article
   */
  public Article setTitle(String title) {
    this.title = title;
    return this;
  }
  
  /**
   * Retrieves this article's content.
   * 
   * @return the article's content
   */
  public String getContent() {
    return content;
  }
  
  /**
   * Sets this article's content.
   * 
   * @param content the article's content
   * @return this article
   */
  public Article setContent(String content) {
    this.content = content;
    return this;
  }
  
  /**
   * Retrieves the creation time of this article.
   * 
   * @return the article's creation time
   */
  public long getTimestamp() {
    return timestamp;
  }
  
  /**
   * Sets the creation time of this article.
   * 
   * @param timestamp the article's creation time
   * @return this article
   */
  public Article setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }
  
}
