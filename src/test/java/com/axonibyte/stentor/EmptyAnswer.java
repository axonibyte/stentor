package com.axonibyte.stentor;

import org.easymock.IAnswer;

/**
 * Denotes an empty answer to be used when mocking methods with void signatures.
 * 
 * @author Caleb L. Power
 */
public class EmptyAnswer implements IAnswer<Object> {
  
  @Override public Object answer() throws Throwable {
    return null;
  }

}
