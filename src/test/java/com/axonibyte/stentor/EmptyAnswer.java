package com.axonibyte.stentor;

import org.easymock.IAnswer;

public class EmptyAnswer implements IAnswer<Object> {

  @Override public Object answer() throws Throwable {
    return null;
  }

}
