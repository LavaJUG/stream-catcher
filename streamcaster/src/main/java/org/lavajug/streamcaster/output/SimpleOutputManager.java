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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.lavajug.streamcaster.profiles.ProfileManager;
import org.lavajug.streamcaster.server.Configuration;
import org.lavajug.streamcaster.server.SystemUtils;
import org.lavajug.streamcaster.server.streamcaster.errors.ErrorBufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sylvain Desgrais <sylvain.desgrais@gmail.com>
 */
public class SimpleOutputManager implements OutputManager {

  private final ProfileManager profileManager;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private ScheduledFuture<?> running;
  
  private Broadcast runningInstance;

  private boolean isRunning = false;

  private String device;

  private static final Logger log = LoggerFactory.getLogger(SimpleOutputManager.class);

  /**
   *
   * @param profileManager
   */
  public SimpleOutputManager(ProfileManager profileManager) {
    this.profileManager = profileManager;
  }

  /**
   *
   * @return
   */
  @Override
  public String getDevice() {
    return device;
  }

  private class Broadcast implements Runnable {

    private final OutputDevice loopback;

    public Broadcast(String device) throws OutputException {
      if(SystemUtils.IS_OS_WINDOWS){
        this.loopback = new WindowsLoopback(device);  
      }else{
        this.loopback = new V4L2Loopback(device, 1024, 768);
      }      
      this.loopback.open();
    }

    @Override
    public void run() {
      String profile = Configuration.getCurrentProfile();
      try {
        if (profile != null) {
          this.loopback.write(profileManager.render(profile));
        } else {
          this.loopback.write(new ErrorBufferedImage("Profile doesn't exists", 1024, 768));
        }
      } catch (OutputException e) {
          try {
            this.loopback.close();
          } catch (OutputException e2) {
              //ignore
          }  
          log.error(e.getLocalizedMessage());
          stop();                  
      }
    }

    protected void destroy() throws OutputException {
      this.loopback.close();
    }

  }

  /**
   *
   * @param device
   * @throws OutputException
   */
  @Override
  public void start(String device) throws OutputException {
    if (isRunning) {
      return;
    }
    this.device = device;
    this.runningInstance = new Broadcast(device);
    this.running = scheduler.scheduleAtFixedRate(this.runningInstance, 0, 60, TimeUnit.MILLISECONDS);
    isRunning = true;

  }

  /**
   *
   */
  @Override
  public void stop() {
    if (!isRunning) {
      return;
    }
    this.running.cancel(true);
    try{
      this.runningInstance.destroy();
    }catch(OutputException ex){
      log.error(ex.getLocalizedMessage());
    }
    isRunning = false;
  }

  /**
   *
   * @return
   */
  @Override
  public boolean isRunning() {
    return isRunning;
  }

}
