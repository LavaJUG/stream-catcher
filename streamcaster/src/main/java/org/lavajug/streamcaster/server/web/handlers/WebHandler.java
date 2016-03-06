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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lavajug.streamcaster.annotations.GET;
import org.lavajug.streamcaster.annotations.POST;
import org.lavajug.streamcaster.reflexion.Reflexion;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.web.Controller;
import org.lavajug.streamcaster.server.web.Router;
import org.lavajug.streamcaster.server.web.WebContext;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class WebHandler implements HttpHandler {

  private final ClassLoaderTemplateResolver templateResolver;

  private final TemplateEngine templateEngine;

  private final Router router;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebHandler.class);

  /**
   *
   */
  public WebHandler() {
    this.router = new Router();
    this.templateEngine = new TemplateEngine();
    this.templateResolver = new ClassLoaderTemplateResolver();
    createDefaultConfiguration();
    initRouter();
  }

  /**
   *
   * @param request
   * @param response
   * @param control
   * @throws Exception
   */
  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
    String uri = request.uri();
    if (uri.contains("?")) {
      uri = uri.substring(0, uri.indexOf('?'));
    }
    Router.Route route = this.router.getRoute(uri, request.method());

    WebContext context = new WebContext();
    context.addParameter("profileManager", Configuration.getProfileManager());
    context.addParameter("sourceManager", Configuration.getSourceManager());
    context.addParameter("pluginManager", Configuration.getPluginManager());
    context.addParameter("outputManager", Configuration.getOutputManager());

    if (route == null) {
      StringWriter writer = new StringWriter();
      templateEngine.process("404", context, writer);
      response.header("Content-Type", "text/html").content(writer.toString()).end();
      return;
    }
    Class<?> clazz = route.getClazz();
    Controller controller = (Controller) clazz.newInstance();

    controller.setContext(context);
    controller.setCurrentPage(uri);
    controller.setRenderer(templateEngine);
    controller.setRequest(request);
    controller.setResponse(response);
    route.getAction().invoke(controller);
  }

  private void createDefaultConfiguration() {
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode("HTML5");
    templateResolver.setCacheable(false);
    templateEngine.setTemplateResolver(templateResolver);
  }

  private void initRouter() {
    try {
      for (Class<?> clazz : Reflexion.getClassesThatExtends(Controller.class)) {
        for (Method method : clazz.getDeclaredMethods()) {
          if (method.isAnnotationPresent(POST.class)) {
            POST post = method.getAnnotation(POST.class);
            this.router.addRoute(new Router.Route(post.value(), "POST", clazz, method));
          }
          if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            this.router.addRoute(new Router.Route(get.value(), "GET", clazz, method));
          }
        }
      }
    } catch (IOException | URISyntaxException ex) {
      log.error(ex.getLocalizedMessage());
    }
  }
}
