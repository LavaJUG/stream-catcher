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

import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.lavajug.streamcaster.annotations.ConfigParameter;
import org.lavajug.streamcaster.annotations.ConfigSelect;
import org.lavajug.streamcaster.annotations.Every;
import org.lavajug.streamcaster.annotations.SourcePlugin;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
@SourcePlugin("webcam")
@Every(50)
public class WebcamPlugin extends AbstractPlugin {

  private Webcam webcam;

  @ConfigParameter(name = "device", description = "Source name")
  @ConfigSelect(method = "selectDevice")
  private String deviceName;

  /**
   *
   * @return
   */
  public static List<String> selectDevice() {
    List<String> devices = new ArrayList<>();
    for (Webcam wc : Webcam.getWebcams()) {
      devices.add(wc.getName());
    }
    return devices;
  }

  /**
   *
   */
  @Override
  public void init() {

    for (Webcam wc : Webcam.getWebcams()) {
      if (wc.getName().replaceAll(" ", "").equals(deviceName.replaceAll(" ", ""))) {
        webcam = wc;
      }
    }
    Dimension preferred = null;
    for (Dimension dimension : webcam.getViewSizes()) {
      if (dimension.getWidth() < 1025) {
        preferred = dimension;
      }
    }
    if (preferred != null) {
      webcam.setViewSize(preferred);
    }
    webcam.open();
  }

  /**
   *
   * @return
   */
  @Override
  public BufferedImage produce() {
    return webcam.getImage();
  }

  /**
   *
   */
  @Override
  public void destroy() {
    if (webcam != null) {
      webcam.close();
    }
  }

}
