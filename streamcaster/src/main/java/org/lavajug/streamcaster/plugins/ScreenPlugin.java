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

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.lavajug.streamcaster.annotations.ConfigParameter;
import org.lavajug.streamcaster.annotations.ConfigSelect;
import org.lavajug.streamcaster.annotations.Every;
import org.lavajug.streamcaster.annotations.SourcePlugin;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
@SourcePlugin("screen")
@Every(500)
public class ScreenPlugin extends AbstractPlugin {

  @ConfigParameter(name = "display", description = "select screen")
  @ConfigSelect(method = "selectDisplay")
  private String display;

  /**
   *
   * @return
   */
  public static List<String> selectDisplay() {
    List<String> screens = new ArrayList<>();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (GraphicsDevice device : ge.getScreenDevices()) {
      screens.add(device.getIDstring().replace('\\', '/'));
    }
    return screens;
  }

  /**
   *
   */
  @Override
  public void init() {
  }

  /**
   *
   * @return
   */
  @Override
  public BufferedImage produce() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice current = null;
    for (GraphicsDevice device : ge.getScreenDevices()) {
      if (device.getIDstring().replace('\\', '/').equals(display)) {
        current = device;
      }
    }
    if (current == null) {
      return new ErrorBufferedImage("Screen device not found : " + display);
    }

    int width = current.getDefaultConfiguration().getBounds().width;
    int height = current.getDefaultConfiguration().getBounds().height;
    try {
      Robot robot = new Robot(current);
      BufferedImage desktop = robot.createScreenCapture(new Rectangle(width, height));
      return desktop;
    } catch (AWTException ex) {
      return new ErrorBufferedImage("Screen capture error : " + ex.getLocalizedMessage());
    }
  }

  /**
   *
   */
  @Override
  public void destroy() {
  }

}
