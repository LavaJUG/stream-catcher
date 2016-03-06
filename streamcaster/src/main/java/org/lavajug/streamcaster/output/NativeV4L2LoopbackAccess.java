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

import com.sun.jna.Library;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public interface NativeV4L2LoopbackAccess extends Library {

  /**
   *
   * @param device
   * @return
   */
  int close_device(int device);

  /**
   *
   * @param path
   * @param w
   * @param h
   * @return
   */
  int open_device(String path, int w, int h);

  /**
   *
   * @param device
   * @param buffer
   * @param length
   * @return
   */
  int writeData(int device, byte[] buffer, int length);
}
