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
package org.lavajug.streamcaster.server.web.handlers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class StreamWebsocketHandler extends BaseWebSocketHandler {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  private final Map<WebSocketConnection, ScheduledFuture<?>> running = new ConcurrentHashMap<>();

  private class Preview implements Runnable {

    private final WebSocketConnection connection;

    private final String section;

    private final String name;

    public Preview(WebSocketConnection connection, String section, String name) {
      this.connection = connection;
      this.name = name;
      this.section = section;
    }

    @Override
    public void run() {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedImage image = null;
      switch (section) {
        case "SOURCES":
          image = Configuration.getSourceManager().render(name);
          break;
        case "PROFILES":
          image = Configuration.getProfileManager().render(name);
          break;
        case "LIVE":
          String profile = Configuration.getCurrentProfile();
          if (profile != null) {
            image = Configuration.getProfileManager().render(profile);
          } else {
            image = new ErrorBufferedImage("Profile doesn't exists");
          }
          break;
      }
      if (image != null) {
        try {
          writeJPG(image,baos,0.9f);
          //ImageIO.write(image, "png", baos);
          String b64image = Base64Coder.encodeLines(baos.toByteArray());
          connection.send(b64image);
        } catch (IOException e) {
        }
      }
    }

  }

  public static void writeJPG(
          BufferedImage bufferedImage,
          OutputStream outputStream,
          float quality) throws IOException {
    Iterator<ImageWriter> iterator
            = ImageIO.getImageWritersByFormatName("jpg");
    ImageWriter imageWriter = iterator.next();
    ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
    imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    imageWriteParam.setCompressionQuality(quality);
    ImageOutputStream imageOutputStream
            = new MemoryCacheImageOutputStream(outputStream);
    imageWriter.setOutput(imageOutputStream);
    IIOImage iioimage = new IIOImage(bufferedImage, null, null);
    imageWriter.write(null, iioimage, imageWriteParam);
    imageOutputStream.flush();
  }

  /**
   *
   * @param connection
   * @param msg
   * @throws Throwable
   */
  @Override
  public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
    if (msg.startsWith("CHANGE-PROFILE")) {
      String[] infos = msg.split(":");
      Configuration.setCurrentProfile(infos[1]);
      return;
    }
    String[] infos = msg.split(":");
    if (!infos[1].equals("ADD")) {
      ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(new Preview(connection, infos[0], infos[1]), 0, 100, TimeUnit.MILLISECONDS);
      this.running.put(connection, scheduled);
    }
  }

  @Override
  public void onClose(WebSocketConnection connection) throws Exception {
    if (this.running.containsKey(connection)) {
      this.running.get(connection).cancel(true);
      this.running.remove(connection);
    }

  }

  /**
   *
   * @param connection
   * @throws Exception
   */
  @Override
  public void onOpen(WebSocketConnection connection) throws Exception {

  }

}
