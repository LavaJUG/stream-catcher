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

import java.io.StringWriter;
import org.thymeleaf.TemplateEngine;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public abstract class Controller {

  /**
   *
   */
  protected HttpRequest request;

  /**
   *
   */
  protected HttpResponse response;

  /**
   *
   */
  protected WebContext context;

  /**
   *
   */
  protected TemplateEngine renderer;

  /**
   *
   */
  protected String currentPage;

  /**
   *
   */
  public void render() {
    StringWriter writer = new StringWriter();
    context.addParameter("page", currentPage);
    renderer.process("page-layout", context, writer);
    this.response.header("Content-Type", "text/html").content(writer.toString()).end();
  }

  /**
   *
   */
  public void notFound() {
    StringWriter writer = new StringWriter();
    renderer.process("404", context, writer);
    this.response.header("Context-Type", "text/html").content(writer.toString()).end();
  }

  /**
   *
   * @param location
   */
  public void redirect(String location) {
    StringWriter writer = new StringWriter();
    this.response.header("Location", location).status(302).end();
  }

  /**
   *
   * @param request
   */
  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  /**
   *
   * @param response
   */
  public void setResponse(HttpResponse response) {
    this.response = response;
  }

  /**
   *
   * @param context
   */
  public void setContext(WebContext context) {
    this.context = context;
  }

  /**
   *
   * @param renderer
   */
  public void setRenderer(TemplateEngine renderer) {
    this.renderer = renderer;
  }

  /**
   *
   * @param currentPage
   */
  public void setCurrentPage(String currentPage) {
    this.currentPage = currentPage;
  }

}
