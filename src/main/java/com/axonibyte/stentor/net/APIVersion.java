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
package com.axonibyte.stentor.net;

import com.axonibyte.stentor.net.APIVersion;

/**
 * The version of the API.
 * 
 * @author Caleb L. Power
 */
public enum APIVersion {
  
  /**
   * Major version 1 of the API
   */
  VERSION_1(1),
  
  /**
   * Unknown version of the API
   */
  UNKNOWN_VERSION(0);
  
  private int val;
  
  private APIVersion(int val) {
    this.val = val;
  }
  
  /**
   * Determines the API version from some string
   * 
   * @param val the string value of the API
   * @return the appropriate APIVersion object
   */
  public static APIVersion fromString(String val) {
    try {
      int v = Integer.parseInt(
          val.length() > 1 && val.toLowerCase().charAt(0) == 'v'
              ? val.substring(1) : val);
      for(APIVersion ver : APIVersion.values())
        if(ver.val == v) return ver;
    } catch(NumberFormatException e) { }
    
    return UNKNOWN_VERSION;
  }
  
  /**
   * Determines the numerical major version of the API.
   * 
   * @return some integer denoting the API
   */
  public int getVal() {
    return val;
  }
  
}
