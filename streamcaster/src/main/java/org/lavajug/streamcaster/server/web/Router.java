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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class Router {

  private final List<Route> routes = new ArrayList<>();

  /**
   *
   * @param route
   */
  public void addRoute(Route route) {
    this.routes.add(route);
  }

  /**
   *
   * @param path
   * @param method
   * @return
   */
  public Route getRoute(String path, String method) {
    for (Route route : this.routes) {
      if (route.getPath().equals(path) && route.getMethod().equals(method)) {
        return route;
      }
    }
    return null;
  }

  /**
   *
   */
  public static class Route {

    private final String path;
    private final String method;
    private final Class<?> clazz;
    private final Method action;

    /**
     *
     * @param path
     * @param method
     * @param clazz
     * @param action
     */
    public Route(String path, String method, Class<?> clazz, Method action) {
      this.path = path;
      this.method = method;
      this.clazz = clazz;
      this.action = action;
    }

    /**
     *
     * @return
     */
    public String getPath() {
      return path;
    }

    /**
     *
     * @return
     */
    public String getMethod() {
      return method;
    }

    /**
     *
     * @return
     */
    public Class<?> getClazz() {
      return clazz;
    }

    /**
     *
     * @return
     */
    public Method getAction() {
      return action;
    }

  }

}
