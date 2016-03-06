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
import org.lavajug.streamcaster.profiles.Profile;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.web.Controller;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class ProfilesController extends Controller {

  /**
   *
   */
  @GET("/profiles/add")
  public void add() {
    context.addParameter("section", "PROFILES");
    context.addParameter("current", "ADD");
    render();
  }

  /**
   *
   */
  @POST("/profiles/add")
  public void create() {
    String name = request.postParam("name");
    Map<String, String> config = new HashMap<>();
    for (String elem : request.postParamKeys()) {
      config.put(elem, request.postParam(elem));
    }
    Profile profile = new Profile();
    profile.setName(name);
    Configuration.getProfileManager().addProfile(name, profile);
    redirect("/profiles/edit?name=" + name);
  }

  /**
   *
   */
  @GET("/profiles/edit")
  public void edit() {
    String name = request.queryParam("name");
    context.addParameter("section", "PROFILES");
    context.addParameter("current", name);
    Profile pro = Configuration.getProfileManager().getProfile(name);
    context.addParameter("pro", pro);
    context.addParameter("inUse", pro.getName().equals(Configuration.getCurrentProfile()));
    render();
  }

  /**
   *
   */
  @POST("/profiles/delete")
  public void delete() {
    String name = request.queryParam("name");
    Configuration.getProfileManager().removeProfile(name);
    redirect("/index");
  }

  /**
   *
   */
  @POST("/profiles/layers/add")
  public void addLayer() {
    String name = request.queryParam("name");
    Profile profile = Configuration.getProfileManager().getProfile(name);
    Layer layer = new Layer();
    layer.setName(request.postParam("name"));
    layer.setHeight(Integer.valueOf(request.postParam("height")));
    layer.setWidth(Integer.valueOf(request.postParam("width")));
    layer.setTop(Integer.valueOf(request.postParam("top")));
    layer.setLeft(Integer.valueOf(request.postParam("left")));
    layer.setSourceName(request.postParam("source"));
    profile.addLayer(layer);
    redirect("/profiles/edit?name=" + name);
  }

  /**
   *
   */
  @POST("/profiles/layers/delete")
  public void removeLayer() {
    String name = request.queryParam("name");
    String layerName = request.queryParam("layerName");
    Profile profile = Configuration.getProfileManager().getProfile(name);
    profile.removeLayer(layerName);
    redirect("/profiles/edit?name=" + name);
  }

  /**
   *
   */
  @GET("/profiles/layers/up")
  public void up() {
    String name = request.queryParam("name");
    String layerName = request.queryParam("layerName");
    Profile profile = Configuration.getProfileManager().getProfile(name);
    profile.upLayer(layerName);
    redirect("/profiles/edit?name=" + name);
  }

  /**
   *
   */
  @GET("/profiles/layers/down")
  public void down() {
    String name = request.queryParam("name");
    String layerName = request.queryParam("layerName");
    Profile profile = Configuration.getProfileManager().getProfile(name);
    profile.downLayer(layerName);
    redirect("/profiles/edit?name=" + name);
  }

}
