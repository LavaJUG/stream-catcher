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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;
import org.lavajug.streamcaster.sources.SourceManager;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class SimpleProfileManager implements ProfileManager {

  private final Map<String, Profile> profiles;

  private final SourceManager sourceManager;

  /**
   *
   * @param sourceManager
   */
  public SimpleProfileManager(SourceManager sourceManager) {
    this.profiles = new ConcurrentHashMap<>();
    this.sourceManager = sourceManager;
  }

  /**
   *
   * @param name
   * @param profile
   */
  @Override
  public void addProfile(String name, Profile profile) {
    this.profiles.put(name, profile);
  }

  /**
   *
   * @param name
   */
  @Override
  public void removeProfile(String name) {
    this.profiles.remove(name);
  }

  /**
   *
   * @return
   */
  @Override
  public Collection<String> getProfileNames() {
    return Collections.unmodifiableCollection(this.profiles.keySet());
  }

  /**
   *
   * @param name
   * @return
   */
  @Override
  public Profile getProfile(String name) {
    return this.profiles.get(name);
  }

  /**
   *
   * @param name
   * @return
   */
  @Override
  public BufferedImage render(String name) {
    Profile profile = profiles.get(name);
    if (profile == null) {
      return new ErrorBufferedImage("This profile doesn't exists anymore", 1024, 768);
    }
    if (profile.getLayers().size() > 0) {
      BufferedImage image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = image.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      for (Layer layer : profile.getLayers()) {
        graphics.drawImage(sourceManager.render(layer.getSourceName()), layer.getLeft(), layer.getTop(), layer.getWidth(), layer.getHeight(), null);
      }
      graphics.dispose();
      return image;
    } else {
      return new ErrorBufferedImage("This profile doesn't have any layer", 1024, 768);
    }
  }

}
