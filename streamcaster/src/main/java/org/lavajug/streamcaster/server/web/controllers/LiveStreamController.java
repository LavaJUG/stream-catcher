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
package org.lavajug.streamcaster.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import org.lavajug.streamcaster.annotations.GET;
import org.lavajug.streamcaster.annotations.POST;
import org.lavajug.streamcaster.output.OutputException;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.SystemUtils;
import org.lavajug.streamcaster.server.web.Controller;
import org.lavajug.streamcaster.server.web.SimpleMimeReader;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class LiveStreamController extends Controller {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(LiveStreamController.class);

  /**
   *
   */
  @GET("/")
  public void root() {
    redirect("/index");
  }

  /**
   *
   */
  @GET("/index")
  public void index() {

    LinkedList<String> devices = new LinkedList<>();    
    Path devicesPath = Paths.get("/dev");

    try {
      for (Path device : Files.newDirectoryStream(devicesPath)) {
        if (device.getFileName().toString().startsWith("video")) {
          devices.addFirst(device.toAbsolutePath().toString());
        }
      }
    } catch (IOException ex) {
    }
    context.addParameter("isWindows", SystemUtils.IS_OS_WINDOWS);
    context.addParameter("devices", devices);
    context.addParameter("section", "LIVE");
    context.addParameter("current", Configuration.getCurrentProfile());
    render();
  }

  /**
   *
   */
  @POST("/start")
  public void start() {
    try {
      String device = request.postParam("device");
      Configuration.getOutputManager().start(device);
    } catch (OutputException ex) {
      log.error(ex.getLocalizedMessage());
    }
    redirect("/index");
  }

  /**
   *
   */
  @GET("/stop")
  public void stop() {
    Configuration.getOutputManager().stop();
    redirect("/index");
  }

  /**
   *
   */
  @GET("/config/download")
  public void download() {
    response.header("Content-Type", "application/json");
    String json = Configuration.serialize();
    response.content(json).end();
  }

  /**
   *
   */
  @POST("/config/upload")
  public void upload() {
    SimpleMimeReader reader = new SimpleMimeReader(new ByteArrayInputStream(request.bodyAsBytes()));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (reader.nextPart()) {
      try {
        Configuration.load(reader.getPartDataAsString());
      } catch (ParseException ex) {
        Logger.getLogger(LiveStreamController.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    redirect("/index");
  }

}
