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
package org.lavajug.streamcaster.server.streamcaster.errors;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class ErrorBufferedImage extends BufferedImage {

  /**
   *
   * @param msg
   */
  public ErrorBufferedImage(String msg) {
    this(msg, 640, 480);
  }

  /**
   *
   * @param msg
   * @param width
   * @param height
   */
  public ErrorBufferedImage(String msg, int width, int height) {
    super(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D content = (Graphics2D) this.getGraphics();
    Rectangle2D textsize = content.getFontMetrics().getStringBounds(msg, content);
    content.drawString(msg, (float) (width / 2 - textsize.getWidth() / 2), (float) (height / 2 - textsize.getHeight() / 2));
    content.dispose();
  }

}
