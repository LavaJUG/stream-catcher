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
package org.lavajug.streamcaster.server.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class ExceptionsHandler implements Thread.UncaughtExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ExceptionsHandler.class);

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    log.error(e.getLocalizedMessage());
  }

}
