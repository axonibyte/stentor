/**
 * Copyright (c) 2021 Axonibyte Innovations, LLC. All rights reserved.
 */
package com.axonibyte.stentor.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * Wrapper to present a string as a stream.
 * 
 * @author Caleb L. Power
 */
public class ServerInputStringStream extends ServletInputStream {
  
  private byte[] bytes = null;
  private int lastIndexRetrieved = 0;
  private ReadListener readListener = null;
  
  /**
   * Instantiates the stream.
   * 
   * @param input the string input
   * @throws UnsupportedEncodingException should never be thrown, as UTF-8
   *         charsets should always be supported
   */
  public ServerInputStringStream(String input) throws UnsupportedEncodingException {
    bytes = input.getBytes("UTF-8");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public boolean isFinished() {
    return lastIndexRetrieved == bytes.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override public boolean isReady() {
    return isFinished();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void setReadListener(ReadListener readListener) {
    this.readListener = readListener;
    try {
      if(isFinished()) readListener.onAllDataRead();
      else readListener.onDataAvailable();
    } catch(IOException e) {
      readListener.onError(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public int read() throws IOException {
    if(isFinished()) return -1;
    int i = bytes[lastIndexRetrieved++] & 0xff;
    if(isFinished() && readListener != null) {
      try {
        readListener.onAllDataRead();
      } catch(IOException e) {
        readListener.onError(e);
        throw e;
      }
    }
    return i;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public int available() throws IOException {
    return bytes.length - lastIndexRetrieved - 1;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void close() throws IOException {
    lastIndexRetrieved = bytes.length - 1;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void reset() {
    this.lastIndexRetrieved = 0;
  }

}