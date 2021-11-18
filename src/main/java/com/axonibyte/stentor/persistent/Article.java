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

import java.util.Set;
import java.util.TreeSet;
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
   * Database key associated with article tags (topics).
   */
  public static final String TAGS_KEY = "tags";
  
  /**
   * Database key associated with the article's unique identifier.
   */
  public static final String ID_KEY = "id";
  
  private UUID id = null;
  private UUID author = null;
  private String title = null;
  private String content = null;
  private Set<String> tags = new TreeSet<>();
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
  
  /**
   * Retrieves the tags associated with this article.
   * 
   * @return an ordered set of tags
   */
  public Set<String> getTags() {
    var tags = new TreeSet<String>();
    tags.addAll(this.tags);
    return tags;
  }
  
  /**
   * Sets the tags associated with this article.
   * 
   * @param tags an ordered set of tags
   * @return this article
   */
  public Article setTags(Set<String> tags) {
    this.tags.clear();
    this.tags.addAll(tags);
    return this;
  }
  
}
