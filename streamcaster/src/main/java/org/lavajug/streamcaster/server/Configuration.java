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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lavajug.streamcaster.output.OutputManager;
import org.lavajug.streamcaster.output.SimpleOutputManager;
import org.lavajug.streamcaster.plugins.AnnotationPluginManager;
import org.lavajug.streamcaster.plugins.PluginManager;
import org.lavajug.streamcaster.profiles.Layer;
import org.lavajug.streamcaster.profiles.Profile;
import org.lavajug.streamcaster.profiles.ProfileManager;
import org.lavajug.streamcaster.profiles.SimpleProfileManager;
import org.lavajug.streamcaster.sources.SimpleSourceManager;
import org.lavajug.streamcaster.sources.SourceManager;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class Configuration {

  private static PluginManager pluginManager;

  private static SourceManager sourceManager;

  private static ProfileManager profileManager;

  private static OutputManager outputManager;

  private static final AtomicReference<String> currentProfile = new AtomicReference<>();

  /**
   *
   * @return
   */
  public static PluginManager getPluginManager() {
    if (pluginManager == null) {
      pluginManager = new AnnotationPluginManager(Configuration.class.getClassLoader(), "org.lavajug.streamcaster.plugins");
    }
    return pluginManager;
  }

  /**
   *
   * @return
   */
  public static SourceManager getSourceManager() {
    if (sourceManager == null) {
      sourceManager = new SimpleSourceManager(getPluginManager(), 20);
    }
    return sourceManager;
  }

  /**
   *
   * @return
   */
  public static ProfileManager getProfileManager() {
    if (profileManager == null) {
      profileManager = new SimpleProfileManager(getSourceManager());
    }
    return profileManager;
  }

  /**
   *
   * @return
   */
  public static OutputManager getOutputManager() {
    if (outputManager == null) {
      outputManager = new SimpleOutputManager(getProfileManager());
    }
    return outputManager;
  }

  /**
   *
   * @param profile
   */
  public static void setCurrentProfile(String profile) {
    currentProfile.set(profile);
  }

  /**
   *
   * @return
   */
  public static String getCurrentProfile() {
    return currentProfile.get();
  }

  /**
   *
   * @param config
   * @throws ParseException
   */
  @SuppressWarnings("unchecked")
  public static void load(String config) throws ParseException {
    //clear current config
    for (String profileName : getProfileManager().getProfileNames()) {
      getProfileManager().removeProfile(profileName);
    }
    for (String sourceName : getSourceManager().getSourceNames()) {
      getSourceManager().removeSource(sourceName);
    }
    getOutputManager().stop();
    setCurrentProfile(null);

    JSONParser parser = new JSONParser();
    JSONObject parse = (JSONObject) parser.parse(config);

    JSONArray sources = (JSONArray) parse.get("sources");
    for (Object s : sources) {
      JSONObject source = (JSONObject) s;
      getSourceManager().addSource((String) source.get("name"), source);
    }

    JSONArray plofiles = (JSONArray) parse.get("profiles");
    for (Object p : plofiles) {
      JSONObject profileConfig = (JSONObject) p;
      Profile profile = new Profile();
      profile.setName((String) profileConfig.get("name"));
      JSONArray layers = (JSONArray) profileConfig.get("layers");
      for (Object l : layers) {
        JSONObject layerConfig = (JSONObject) l;
        Layer layer = new Layer();
        layer.setName((String) layerConfig.get("name"));
        layer.setSourceName((String) layerConfig.get("source"));
        layer.setTop(((Long) layerConfig.get("top")).intValue());
        layer.setLeft(((Long) layerConfig.get("left")).intValue());
        layer.setWidth(((Long) layerConfig.get("width")).intValue());
        layer.setHeight(((Long) layerConfig.get("height")).intValue());
        profile.addLayer(layer);
      }
      getProfileManager().addProfile(profile.getName(), profile);
    }

  }

  /**
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static String serialize() {
    JSONObject config = new JSONObject();

    JSONArray sources = new JSONArray();
    for (String sourceName : getSourceManager().getSourceNames()) {
      Map<String, String> sourceConfig = getSourceManager().getSourceConfig(sourceName);
      sources.add(new JSONObject(sourceConfig));
    }
    config.put("sources", sources);

    JSONArray profiles = new JSONArray();
    for (String profilename : getProfileManager().getProfileNames()) {
      Profile profile = getProfileManager().getProfile(profilename);
      JSONObject outProfile = new JSONObject();
      outProfile.put("name", profile.getName());

      JSONArray layers = new JSONArray();
      for (Layer layer : profile.getLayers()) {
        JSONObject outlayer = new JSONObject();
        outlayer.put("name", layer.getName());
        outlayer.put("source", layer.getSourceName());
        outlayer.put("top", layer.getTop());
        outlayer.put("left", layer.getLeft());
        outlayer.put("width", layer.getWidth());
        outlayer.put("height", layer.getHeight());
        layers.add(outlayer);

      }
      outProfile.put("layers", layers);

      profiles.add(outProfile);
    }
    config.put("profiles", profiles);
    return config.toJSONString();
  }

}
