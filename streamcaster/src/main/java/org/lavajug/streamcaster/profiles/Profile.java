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
package org.lavajug.streamcaster.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class Profile {

  private String name;

  private final List<Layer> layers;

  /**
   *
   */
  public Profile() {
    this.layers = new CopyOnWriteArrayList<>();
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @return
   */
  public Collection<Layer> getLayers() {
    return Collections.unmodifiableCollection(layers);
  }

  /**
   *
   * @param layer
   */
  public void addLayer(Layer layer) {
    Layer exists = null;
    for (Layer item : this.layers) {
      if (item.getName().equals(layer.getName())) {
        exists = item;
      }
    }
    if (exists != null) {
      this.layers.remove(exists);
    }
    this.layers.add(layer);
  }

  /**
   *
   * @param name
   */
  public void upLayer(String name) {
    for (int i = 0; i < layers.size(); i++) {
      if (layers.get(i).getName().equals(name) && i > 0) {
        Layer layer = layers.remove(i);
        layers.add(i - 1, layer);
      }
    }
  }

  /**
   *
   * @param name
   */
  public void downLayer(String name) {
    for (int i = 0; i < layers.size(); i++) {
      if (layers.get(i).getName().equals(name) && i < layers.size() - 1) {
        Layer layer = layers.remove(i);
        layers.add(i + 1, layer);
      }
    }
  }

  /**
   *
   * @param name
   */
  public void removeLayer(String name) {
    Layer layer = null;
    for (Layer item : this.layers) {
      if (item.getName().equals(name)) {
        layer = item;
      }
    }
    if (layer != null) {
      this.layers.remove(layer);
    }
  }

}
