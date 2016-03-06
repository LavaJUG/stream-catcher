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
package org.lavajug.streamcaster.sources;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.lavajug.streamcaster.annotations.Every;
import org.lavajug.streamcaster.plugins.Plugin;
import org.lavajug.streamcaster.plugins.PluginManager;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class SimpleSourceManager implements SourceManager {

  private final PluginManager pluginsManager;

  private final Map<String, Plugin> sources;

  private final Map<String, BufferedImage> rendered;

  private final Map<String, ScheduledFuture<?>> running;

  private final ScheduledExecutorService scheduler;

  private final Map<String, Map<String, String>> configs;

  /**
   *
   * @param pluginManager
   * @param threadPoolSize
   */
  public SimpleSourceManager(PluginManager pluginManager, int threadPoolSize) {
    this.pluginsManager = pluginManager;
    this.scheduler = Executors.newScheduledThreadPool(threadPoolSize);
    this.sources = new ConcurrentHashMap<>();
    this.rendered = new ConcurrentHashMap<>();
    this.running = new ConcurrentHashMap<>();
    this.configs = new ConcurrentHashMap<>();
  }

  /**
   *
   * @param name
   * @param config
   */
  @Override
  public void addSource(String name, Map<String, String> config) {
    if (sources.containsKey(name)) {
      removeSource(name);
    }
    Plugin plugin = pluginsManager.getInstance(config);
    long rate = 1000;
    Every every = plugin.getClass().getAnnotation(Every.class);
    if (every != null) {
      rate = every.value();
    }
    sources.put(name, plugin);
    configs.put(name, config);
    plugin.setReciever(name, rendered);
    ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(plugin, 0, rate, TimeUnit.MILLISECONDS);
    this.running.put(name, scheduled);
    rendered.put(name, new ErrorBufferedImage("Loading source..."));
  }

  /**
   *
   * @param name
   */
  @Override
  public void removeSource(String name) {
    if (!sources.containsKey(name)) {
      return;
    }
    sources.get(name).destroy();
    sources.remove(name);
    running.get(name).cancel(true);
    running.remove(name);
    configs.remove(name);
    rendered.remove(name);
  }

  /**
   *
   * @return
   */
  @Override
  public Collection<String> getSourceNames() {
    return Collections.unmodifiableCollection(this.sources.keySet());
  }

  /**
   *
   * @param name
   * @return
   */
  @Override
  public Map<String, String> getSourceConfig(String name) {
    return configs.get(name);
  }

  /**
   *
   * @param name
   * @return
   */
  @Override
  public BufferedImage render(String name) {
    if (this.rendered.containsKey(name)) {
      return this.rendered.get(name);
    }
    return new ErrorBufferedImage("The source '" + name + "' dosen't exists");
  }

}
