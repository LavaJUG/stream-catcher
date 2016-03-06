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
package org.lavajug.streamcaster.reflexion;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lavajug.streamcaster.plugins.AnnotationPluginManager;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class Reflexion {

  public static List<Class<?>> getClassesAnnotatedWith(final Class<? extends Annotation> annotation)
          throws IOException, URISyntaxException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final List<Class<?>> classes = new ArrayList<>();
    if (classLoader instanceof URLClassLoader) {
      URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
      for (URL resource : urlClassLoader.getURLs()) {
        Path directory;
        if (resource.getFile().endsWith(".jar")) {
          System.err.println(resource.getFile());
          Map<String, ?> env = Collections.emptyMap();
          try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:" + resource.toURI().toString()), env)) {
            directory = fileSystem.getPath("/");
            AnnotatedWithVisitor annotatedWithVisitor = new AnnotatedWithVisitor(annotation, directory);
            Files.walkFileTree(directory, annotatedWithVisitor);
            classes.addAll(annotatedWithVisitor.getFound());
          }
        } else {
          directory = Paths.get(resource.toURI());
          AnnotatedWithVisitor annotatedWithVisitor = new AnnotatedWithVisitor(annotation, directory);
          Files.walkFileTree(directory, annotatedWithVisitor);
          classes.addAll(annotatedWithVisitor.getFound());
        }
      }

    }
    return classes;
  }

  public static List<Class<?>> getClassesThatExtends(final Class<?> extended)
          throws IOException, URISyntaxException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final List<Class<?>> classes = new ArrayList<>();
    if (classLoader instanceof URLClassLoader) {
      URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
      for (URL resource : urlClassLoader.getURLs()) {
        Path directory;
        if (resource.getFile().endsWith(".jar")) {
          System.err.println(resource.getFile());
          Map<String, ?> env = Collections.emptyMap();
          try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:" + resource.toURI().toString()), env)) {
            directory = fileSystem.getPath("/");
            ExtendsWithVisitor extendsWithVisitor = new ExtendsWithVisitor(extended, directory);
            Files.walkFileTree(directory, extendsWithVisitor);
            classes.addAll(extendsWithVisitor.getFound());
          }
        } else {
          directory = Paths.get(resource.toURI());
          ExtendsWithVisitor extendsWithVisitor = new ExtendsWithVisitor(extended, directory);
          Files.walkFileTree(directory, extendsWithVisitor);
          classes.addAll(extendsWithVisitor.getFound());
        }
      }

    }
    return classes;
  }

  private static class AnnotatedWithVisitor extends SimpleFileVisitor<Path> {

    private final Class<? extends Annotation> annotation;

    private final Path basePath;

    private final List<Class<?>> found;

    public AnnotatedWithVisitor(Class<? extends Annotation> annotation, Path basePath) {
      this.found = new ArrayList<>();
      this.annotation = annotation;
      this.basePath = basePath;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
      if (attrs.isRegularFile() && file.getFileName().toString().endsWith(".class")) {
        try {
          String name = "";
          for (int i = basePath.getNameCount(); i < file.getNameCount(); i++) {
            name += file.getName(i) + ".";
          }
          if (name.startsWith("org.lavajug")) {
            Class<?> clazz = Class.forName(name.substring(0, name.length() - 7));
            if (clazz.isAnnotationPresent(annotation)) {
              found.add(clazz);
            }
          }
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
          Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      return FileVisitResult.CONTINUE;
    }

    public List<Class<?>> getFound() {
      return this.found;
    }
  }

  private static class ExtendsWithVisitor extends SimpleFileVisitor<Path> {

    private final Class<?> extended;

    private final Path basePath;

    private final List<Class<?>> found;

    public ExtendsWithVisitor(Class<?> extended, Path basePath) {
      this.found = new ArrayList<>();
      this.extended = extended;
      this.basePath = basePath;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
      if (attrs.isRegularFile() && file.getFileName().toString().endsWith(".class")) {
        try {
          String name = "";
          for (int i = basePath.getNameCount(); i < file.getNameCount(); i++) {
            name += file.getName(i) + ".";
          }
          if (name.startsWith("org.lavajug")) {
            Class<?> clazz = Class.forName(name.substring(0, name.length() - 7));
            if (extended.isAssignableFrom(clazz)) {
              found.add(clazz);
            }
          }
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
          Logger.getLogger(AnnotationPluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      return FileVisitResult.CONTINUE;
    }

    public List<Class<?>> getFound() {
      return this.found;
    }
  }

}
