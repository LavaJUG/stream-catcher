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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import org.lavajug.streamcaster.annotations.ConfigParameter;
import org.lavajug.streamcaster.annotations.Every;
import org.lavajug.streamcaster.annotations.SourcePlugin;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
@SourcePlugin("remote")
@Every(700)
public class RemotePlugin extends AbstractPlugin {

  @ConfigParameter(name = "url", description = "Url of the Screen Catcher (ie : http://192.168.1.1:8000/screens/0)")
  private URL url;

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
    try {
      HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
      openConnection.setUseCaches(false);
      openConnection.connect();
      if (openConnection.getResponseCode() != 200) {
        return new ErrorBufferedImage("Unable to connect to remote host : " + openConnection.getResponseMessage());
      }
      InputStream inputStream = openConnection.getInputStream();
      BufferedImage remoteScreen = ImageIO.read(inputStream);
      return remoteScreen;
    } catch (IOException e) {
      return new ErrorBufferedImage("Remote : " + e.getMessage());
    }
  }

  /**
   *
   */
  @Override
  public void destroy() {
  }

}
