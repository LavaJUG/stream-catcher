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
package org.lavajug.streamcaster.output;

import com.sun.jna.Native;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lavajug.streamcaster.server.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class V4L2Loopback implements OutputDevice {

  private final int width;
  private final int height;
  private final String devicePath;
  private int devFD = 0;
  private byte[] rgbs = null;

  private final static NativeV4L2LoopbackAccess INSTANCE;

  private static final Logger log = LoggerFactory.getLogger(V4L2Loopback.class);

  static {
    if(!SystemUtils.IS_OS_WINDOWS){
      try {
        Path tempDir = Files.createTempDirectory("streamcatcher");
        Path lib = tempDir.resolve("libstreamcatcher.so");
        try (OutputStream out = Files.newOutputStream(lib)) {
          InputStream in = V4L2Loopback.class.getResourceAsStream("/libstreamcatcher.so");
          if (in != null) {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
              out.write(data, 0, nRead);
            }
            out.flush();
            System.setProperty("jna.library.path", tempDir.toAbsolutePath().toString());
          }
        }
      } catch (IOException ex) {
        log.error(ex.getLocalizedMessage());
      }
      INSTANCE = (NativeV4L2LoopbackAccess) Native.loadLibrary("streamcatcher", NativeV4L2LoopbackAccess.class);
    }else{
      INSTANCE = null;
    }
  }

  /**
   *
   * @param path
   * @param width
   * @param height
   */
  public V4L2Loopback(String path, int width, int height) {
    this.width = width;
    this.height = height;
    this.devicePath = path;
  }

  /**
   *
   * @throws OutputException
   */
  @Override
  public void open() throws OutputException {
    int result = INSTANCE.open_device(devicePath, width, height);
    if (result <= 0) {
      throw new OutputException("(" + result + ") Error opening device : " + devicePath);
    }
    this.devFD = result;
  }

  /**
   *
   * @throws OutputException
   */
  @Override
  public void close() throws OutputException {
    int result = INSTANCE.close_device(devFD);
    if (result <= 0) {
      throw new OutputException("(" + result + ") Error opening device : " + devicePath);
    }
  }

  /**
   *
   * @param data
   * @throws OutputException
   */
  @Override
  public void write(BufferedImage data) throws OutputException {
    if (data.getType() != BufferedImage.TYPE_INT_RGB) {
      throw new OutputException("Output device accepts only BufferedImage of type BufferedImage.TYPE_INT_RGB");
    }
    if (devFD != 0) {
      Raster raster = data.getData();
      Object out = null;
      out = raster.getDataElements(0, 0, width, height, out);
      int[] ints = (int[]) out;
      byte[] buffer = img2rgb24(ints);
      int countWritten = INSTANCE.writeData(devFD, buffer, buffer.length);
      if (countWritten != buffer.length) {
        throw new OutputException("Error writing datas to device : " + devicePath);
      }
    }
  }

  private byte[] img2rgb24(int[] data) {
    if (rgbs == null || rgbs.length != data.length * 3) {
      rgbs = new byte[data.length * 3];
    }
    int index = 0;
    for (int i = 0; i < data.length; i++) {      
      rgbs[index++] = (byte) (data[i] >> 16 & 0xFF);
      rgbs[index++] = (byte) (data[i] >> 8 & 0xFF);
      rgbs[index++] = (byte) (data[i] & 0xFF);
    }
    return rgbs;
  }

}
