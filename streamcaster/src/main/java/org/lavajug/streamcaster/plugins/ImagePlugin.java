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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.lavajug.streamcaster.annotations.ConfigParameter;
import org.lavajug.streamcaster.annotations.SourcePlugin;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
@SourcePlugin("image")
public class ImagePlugin extends AbstractPlugin {

  @ConfigParameter(name = "file", description = "File path to the image on the server")
  private String path;

  private BufferedImage image;

  /**
   *
   */
  @Override
  public void init() {
    try {
      Path imageFile = Paths.get(path);
      image = toCompatibleImage(ImageIO.read(Files.newInputStream(imageFile)));
    } catch (IOException ex) {
      image = new ErrorBufferedImage("Error when loading image : " + ex.getLocalizedMessage());
    }
  }

  private BufferedImage toCompatibleImage(BufferedImage image) {
    BufferedImage new_image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = (Graphics2D) new_image.getGraphics();
    g2d.drawImage(image, 0, 0, null);
    g2d.dispose();
    return new_image;
  }

  /**
   *
   * @return
   */
  @Override
  public BufferedImage produce() {
    return image;
  }

  /**
   *
   */
  @Override
  public void destroy() {
  }

}
