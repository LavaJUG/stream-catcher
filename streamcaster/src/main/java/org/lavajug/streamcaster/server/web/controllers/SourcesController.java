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

import java.util.HashMap;
import java.util.Map;
import org.lavajug.streamcaster.annotations.GET;
import org.lavajug.streamcaster.annotations.POST;
import org.lavajug.streamcaster.profiles.Layer;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.web.Controller;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class SourcesController extends Controller {

  /**
   *
   */
  @GET("/sources/add")
  public void add() {
    context.addParameter("section", "SOURCES");
    context.addParameter("current", "ADD");
    render();
  }

  /**
   *
   */
  @POST("/sources/add")
  public void create() {
    String name = request.postParam("name");
    Map<String, String> config = new HashMap<>();
    for (String elem : request.postParamKeys()) {
      config.put(elem, request.postParam(elem));
    }
    Configuration.getSourceManager().addSource(name, config);
    redirect("/sources/edit?name=" + name);
  }

  /**
   *
   */
  @GET("/sources/config")
  public void config() {
    response.header("Content-Type", "application/json");
    String json = Configuration.getPluginManager().getConfigDescription(request.queryParam("type"));
    response.content(json).end();
  }

  /**
   *
   */
  @GET("/sources/edit")
  public void edit() {
    String name = request.queryParam("name");
    if (name == null) {
      redirect("/index");
    } else {
      context.addParameter("config", Configuration.getSourceManager().getSourceConfig(name));
      context.addParameter("name", name);
      context.addParameter("section", "SOURCES");
      context.addParameter("current", name);
      context.addParameter("inUse", isSourceInUse(name));
      render();
    }
  }

  private boolean isSourceInUse(String souceName) {
    if (Configuration.getCurrentProfile() == null) {
      return false;
    }
    for (Layer layer : Configuration.getProfileManager().getProfile(Configuration.getCurrentProfile()).getLayers()) {
      if (layer.getSourceName().equals(souceName)) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   */
  @POST("/sources/edit")
  public void save() {
    String name = request.queryParam("name");
    if (name == null) {
      redirect("/index");
    } else {
      String updatedName = request.postParam("name");
      if (!updatedName.equals(name)) {
        Configuration.getSourceManager().removeSource(name);
      }
      Map<String, String> config = new HashMap<>();
      for (String elem : request.postParamKeys()) {
        config.put(elem, request.postParam(elem));
      }
      Configuration.getSourceManager().addSource(updatedName, config);
      redirect("/sources/edit?name=" + updatedName);
    }
  }

  /**
   *
   */
  @POST("/sources/delete")
  public void delete() {
    String name = request.queryParam("name");
    if (name == null) {
      redirect("/index");
    } else {
      Configuration.getSourceManager().removeSource(name);
      redirect("/index");
    }
  }

}
