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
package org.lavajug.streamcaster.server.web;

import java.util.Locale;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class WebContext implements IContext {

  private final VariablesMap<String, Object> variables;

  /**
   *
   */
  public WebContext() {
    this.variables = new VariablesMap<>();
  }

  /**
   *
   * @return
   */
  @Override
  public VariablesMap<String, Object> getVariables() {
    return this.variables;
  }

  /**
   *
   * @return
   */
  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }

  /**
   *
   * @param string
   */
  @Override
  public void addContextExecutionInfo(String string) {

  }

  /**
   *
   * @param name
   * @param value
   */
  public void addParameter(String name, Object value) {
    this.variables.put(name, value);
  }

}
