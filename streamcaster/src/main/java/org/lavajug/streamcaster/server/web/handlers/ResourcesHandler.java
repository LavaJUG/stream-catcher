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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class ResourcesHandler implements HttpHandler {

  private final Map<String, String> mimeTypes = new HashMap<>();

  /**
   *
   */
  public ResourcesHandler() {
    mimeTypes.put(".css", "text/css");
    mimeTypes.put(".js", "text/javascript");
    mimeTypes.put(".png", "image/png");
    mimeTypes.put(".jpg", "image/jpeg");
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
    String path = request.uri();
    path = path.substring("/resources".length());
    if (path.contains("?")) {
      path = path.substring(0, path.indexOf('?'));
    }
    String mime = path.substring(path.lastIndexOf('.'));
    InputStream resource = ResourcesHandler.class.getResourceAsStream("/assets" + path);
    if (resource == null) {
      StringWriter writer = new StringWriter();
      response.header("Content-Type", "text/plain").content(path + " : resource not found").status(404).end();
      return;
    }
    int nRead;
    byte[] data = new byte[1024];
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    while ((nRead = resource.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();
    response.header("Content-Type", mimeTypes.get(mime)).content(buffer.toByteArray()).end();
  }

}
