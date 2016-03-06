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
package org.lavajug.streamcaster.plugins;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lavajug.streamcaster.annotations.ConfigParameter;
import org.lavajug.streamcaster.annotations.ConfigSelect;
import org.lavajug.streamcaster.annotations.SourcePlugin;
import org.lavajug.streamcaster.reflexion.Reflexion;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class AnnotationPluginManager implements PluginManager {

  private final Map<String, Class<?>> plugins;

  /**
   * return a new instance of the default PluginManager
   *
   * @param classLoader containing plugins class
   * @param searchPath where to find plugins classes
   */
  public AnnotationPluginManager(ClassLoader classLoader, String... searchPath) {
    this.plugins = new HashMap<>();
    try {
      for (Class<?> clazz : Reflexion.getClassesAnnotatedWith(SourcePlugin.class)) {
        if (clazz.isAnnotationPresent(SourcePlugin.class)) {
          SourcePlugin ann = clazz.getAnnotation(SourcePlugin.class);
          plugins.put(ann.value(), clazz);
        }
      }
    } catch (IOException | URISyntaxException ex) {
      Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * return a configured instance of the current plugin
   *
   * @param config parameters for this instance
   * @return a configured instance of the current plugin
   */
  @Override
  public Plugin getInstance(Map<String, String> config) {
    String type = config.get("type");
    Plugin instance = null;
    try {
      instance = (Plugin) plugins.get(type).newInstance();
      for (Field field : instance.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(ConfigParameter.class)) {
          ConfigParameter ann = field.getAnnotation(ConfigParameter.class);
          if (config.containsKey(ann.name())) {
            setValue(instance, field, config.get(ann.name()));
          }
        }
      }
    } catch (InstantiationException | IllegalAccessException ex) {
      Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return instance;
  }

  /**
   * return a JSON representation of the plugin configuration
   *
   * @param name of the plugin
   * @return JSON representation of the plugin configuration
   */
  @Override
  @SuppressWarnings("unchecked")
  public String getConfigDescription(String name) {

    JSONArray parameters = new JSONArray();

    try {
      Class<?> clazz = this.plugins.get(name);
      if (clazz != null) {
        for (Field field : clazz.getDeclaredFields()) {
          if (field.isAnnotationPresent(ConfigParameter.class)) {

            JSONObject fieldConfig = new JSONObject();

            ConfigParameter ann = field.getAnnotation(ConfigParameter.class);

            fieldConfig.put("name", ann.name());
            fieldConfig.put("description", ann.description());

            if (field.isAnnotationPresent(ConfigSelect.class)) {

              fieldConfig.put("type", "select");

              ConfigSelect select = field.getAnnotation(ConfigSelect.class);
              Method method = clazz.getMethod(select.method());
              List<String> values = (List<String>) method.invoke(null);

              JSONArray valuesArray = new JSONArray();
              for (String value : values) {
                valuesArray.add(value);
              }

              fieldConfig.put("values", valuesArray);

            } else {
              fieldConfig.put("type", "text");
            }
            parameters.add(fieldConfig);
          }
        }
      }
    } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    }
    return parameters.toJSONString();
  }

  /**
   * return a list of names of installed plugins
   *
   * @return a list of names of installed plugins
   */
  @Override
  public Collection<String> getPluginNames() {
    return Collections.unmodifiableCollection(this.plugins.keySet());
  }

  private void setValue(Plugin instance, Field field, String value) {
    try {
      field.setAccessible(true);
      if (field.getType() == URL.class) {
        try {
          field.set(instance, new URL(value));
        } catch (MalformedURLException ex) {
          Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      } else if (field.getType() == int.class || field.getType() == Integer.class) {
        field.set(instance, Integer.parseInt(value));
      } else if (field.getType() == long.class || field.getType() == Long.class) {
        field.set(instance, Long.parseLong(value));
      } else if (field.getType() == double.class || field.getType() == Double.class) {
        field.set(instance, Double.parseDouble(value));
      } else if (field.getType() == Path.class) {
        field.set(instance, Paths.get(value));
      } else {
        field.set(instance, value);
      }
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /*private static List<Class> getClassesAnnotatedWith(final Class<? extends Annotation> annotation)
          throws ClassNotFoundException, IOException, URISyntaxException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final List<Class> classes = new ArrayList<>();
    while (classLoader != null) {
      if (classLoader instanceof URLClassLoader) {
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        for (URL resource : urlClassLoader.getURLs()) {
          final Path directory = Paths.get(resource.toURI());
          System.err.println(directory);
          Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attr) {
              System.err.println(file);
              if (attr.isRegularFile() && file.getFileName().toString().endsWith(".class")) {
                try {
                  String name = "";
                  for (int i = directory.getNameCount(); i < file.getNameCount(); i++) {
                    name += file.getName(i) + ".";
                  }
                  System.err.println(name.substring(0, name.length() - 7));
                  Class<?> clazz = Class.forName(name.substring(0, name.length() - 7));
                  if (clazz.isAnnotationPresent(annotation)) {
                    classes.add(clazz);
                    System.err.println(clazz);
                  }
                } catch (ClassNotFoundException ex) {
                  Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
              return FileVisitResult.CONTINUE;
            }
          });
        }

      }
      classLoader = classLoader.getParent();
    }
    return classes;
  }*/

}
