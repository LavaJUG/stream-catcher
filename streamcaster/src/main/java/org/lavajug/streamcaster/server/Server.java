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
package org.lavajug.streamcaster.server;

import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import org.json.simple.parser.ParseException;
import org.lavajug.streamcaster.server.web.handlers.ExceptionsHandler;
import org.lavajug.streamcaster.server.web.handlers.ResourcesHandler;
import org.lavajug.streamcaster.server.web.handlers.StreamWebsocketHandler;
import org.lavajug.streamcaster.server.web.handlers.WebHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class Server {

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    int port = 7777;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException ex) {
        showHelp();
        System.out.println(ex.getLocalizedMessage());
        System.exit(1);
      }
    }

    if (args.length > 1) {
      try {
        Configuration.load(args[1]);
      } catch (ParseException ex) {
        showHelp();
        System.out.println(ex.getLocalizedMessage());
        System.exit(1);
      }
    }

    Iterator<ImageWriter> iterator
            = ImageIO.getImageWritersByFormatName("png");
    while (iterator.hasNext()) {
      ImageWriter next = iterator.next();
      System.err.println(next.getOriginatingProvider().getDescription(Locale.getDefault()));
    }

    WebServer webServer = WebServers.createWebServer(port);
    webServer.add("/socket", new StreamWebsocketHandler());
    webServer.add("/resources/.*", new ResourcesHandler());
    webServer.add("/.*", new WebHandler());
    webServer.uncaughtExceptionHandler(new ExceptionsHandler());
    webServer.start();
  }

  private static void showHelp() {
    System.out.println("Usage :");
    System.out.println("  java -jar stream-caster.jar [port] [config file]");
    System.out.println();
  }

}
