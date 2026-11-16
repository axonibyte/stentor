/*
 * Copyright (c) 2020 Axonibyte Innovations, LLC
 * 
 *   Licensed under the Blue Oak Model License 1.0.0.
 *   See LICENSE for the full terms.
 */
package com.axonibyte.stentor.log;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple logger.
 * 
 * @author Caleb L. Power
 */
public class Logger {
  
  private static enum LogLevel {
    DEBUG, INFO, ERROR
  }
  
  private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
  
  // TODO make the timezone configurable maybe
  private static Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
  
  /**
   * Prints a debugging message.
   * 
   * @param label the label or relevant module
   * @param message the message
   */
  public static void onDebug(String label, String message) {
    log(System.out, LogLevel.DEBUG, label, message);
  }
  
  /**
   * Prints an informational message.
   * 
   * @param label the label or relevant module
   * @param message the message
   */
  public static void onInfo(String label, String message) {
    log(System.out, LogLevel.INFO, label, message);
  }
  
  /**
   * Prints an error message.
   * 
   * @param label the label or relevant module
   * @param message the message
   */
  public static void onError(String label, String message) {
    log(System.err, LogLevel.ERROR, label, message);
  }
  
  private static void log(PrintStream out, LogLevel level, String label, String message) {
    calendar.setTimeInMillis(System.currentTimeMillis());
    System.out.printf("[%1$s: %2$s] %3$s - %4$s\n",
        sdf.format(calendar.getTime()),
        level.name(),
        label,
        message);
  }
  
}
