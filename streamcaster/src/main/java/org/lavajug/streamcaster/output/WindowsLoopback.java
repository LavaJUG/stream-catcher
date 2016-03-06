/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lavajug.streamcaster.output;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class WindowsLoopback implements OutputDevice {

  private static final Logger log = LoggerFactory.getLogger(WindowsLoopback.class);    
    
  private RandomAccessFile pipe;
  
  private byte[] rgbs = null;
  
  private final String device;

  public WindowsLoopback(String device) {
    this.device = device;
  }
  
  
  @Override
  public void open() throws OutputException {
    try {
      this.pipe = new RandomAccessFile("\\\\.\\pipe\\streamcatcher", "rw");
    } catch (FileNotFoundException ex) {
      throw new OutputException("Failed to open the pipe", ex);
    }
  }

  @Override
  public void close() throws OutputException {
    try {
      this.pipe.close();
    } catch (IOException ex) {
      throw new OutputException("Failed to close the pipe",ex);
    }
  }

  @Override
  public void write(BufferedImage data) throws OutputException {
    if (data.getType() != BufferedImage.TYPE_INT_RGB) {
      throw new OutputException("Output device accepts only BufferedImage of type BufferedImage.TYPE_INT_RGB");
    }
    if (pipe != null) {
      Raster raster = data.getData();
      Object out = null;
      out = raster.getDataElements(0, 0, 1024, 768, out);
      int[] ints = (int[]) out;
      byte[] buffer = img2rgb24(ints);
      try {
        pipe.write(buffer);
      } catch (IOException ex) {
        throw new OutputException("Failed to write datas in the pipe",ex);
      }
    }    
  }
  
  private byte[] img2rgb24(int[] data) {
    if (rgbs == null || rgbs.length != data.length * 3) {
      rgbs = new byte[data.length * 3];
    }
    int index = 0;
    
    for(int i = 767 ; i >= 0 ; i--){
        for(int j = 0 ; j < 1024;j++){
            rgbs[index++] = (byte) (data[i*1024+j] & 0xFF);      
            rgbs[index++] = (byte) (data[i*1024+j] >> 8 & 0xFF);
            rgbs[index++] = (byte) (data[i*1024+j] >> 16 & 0xFF);             
        }
    }
    return rgbs;
  }  
  
}
