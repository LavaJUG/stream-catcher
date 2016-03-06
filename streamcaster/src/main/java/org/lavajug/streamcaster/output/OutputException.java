/* 
 * Copyright 2014 LavaJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lavajug.streamcaster.output;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class OutputException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * dummy contructor
   */
  public OutputException() {
    super();
  }

  /**
   * message contructor
   * 
   * @param message
   */
  public OutputException(String message) {
    super(message);
  }

  /**
   * from previous exception constructor
   * 
   * @param parent exception
   */
  public OutputException(Throwable parent) {
    super(parent);
  }

  /**
   * from previous exception constructor with a message
   * 
   * @param message the message
   * @param parent exception
   */
  public OutputException(String message, Throwable parent) {
    super(message, parent);
  }

}
