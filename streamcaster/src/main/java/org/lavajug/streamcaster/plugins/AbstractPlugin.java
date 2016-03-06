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

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public abstract class AbstractPlugin implements Plugin {

  private boolean initialized = false;

  private Map<String, BufferedImage> reciever;

  private String name;

  @Override
  public final void run() {
    if (!initialized) {
      this.init();
      this.initialized = true;
    }
    send(this.produce());
  }

  /**
   *
   * @param name of the source
   * @param reciever is the container where put the image when it is computed
   */
  @Override
  public final void setReciever(String name, Map<String, BufferedImage> reciever) {
    this.reciever = reciever;
    this.name = name;
  }

  /**
   * set the image in the container when it is ready to use
   * 
   * @param image computed
   */
  protected final void send(BufferedImage image) {
    this.reciever.put(name, image);
  }

}
