package org.lavajug;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.imageio.ImageIO;

/**
 *
 * @author sylvain
 */
public class Server {

  private static final String err404 = "Resource Not Found";

  private static final String err405 = "Method Not Allowed";

  private static final String err500 = "Ooops! ";

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/", new ScreenHandler());
    //server.setExecutor(null); 
    server.start();

  }

  static class ScreenHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
      if (!he.getRequestMethod().equalsIgnoreCase("GET")) {
        notAllowed(he);
        return;
      }
      String path = he.getRequestURI().getPath();
      if (path.equals("/infos")) {
        processInfos(he);
        return;
      }
      if (path.startsWith("/screens/")) {
        String screenId = path.substring("/screens/".length());
        try {
          processCapture(he, Integer.parseInt(screenId));
        } catch (AWTException ex) {
          error(he, ex);
        } catch (NumberFormatException ex) {
          error(he, ex);
        }
        return;
      }

      notFound(he);

    }

    private void processInfos(HttpExchange he) throws IOException {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      String infos = "{\n\t\"screens\" : [\n";
      int id = 0;
      for (GraphicsDevice device : ge.getScreenDevices()) {
        infos += "\t\t{\n";
        infos += "\t\t\t\"id\" : " + id + ",\n";
        infos += "\t\t\t\"name\" : \"" + device.getIDstring() + "\",\n";
        switch (device.getType()) {
          case GraphicsDevice.TYPE_RASTER_SCREEN:
            infos += "\t\t\t\"type\" : \"screen\",\n";
            break;
          case GraphicsDevice.TYPE_PRINTER:
            infos += "\t\t\t\"type\" : \"printer\",\n";
            break;
          case GraphicsDevice.TYPE_IMAGE_BUFFER:
            infos += "\t\t\t\"type\" : \"image\",\n";
            break;
          default:
            infos += "\t\t\t\"type\" : \"unknown\",\n";
            break;
        }
        infos += "\t\t\t\"uri\" : \"/screens/" + id + "\"\n";
        infos += "\t\t}" + (id < ge.getScreenDevices().length - 1 ? "," : "") + "\n";
        id++;
      }
      infos += "\t]\n}";
      he.getResponseHeaders().add("Content-Type", "application/json");
      he.sendResponseHeaders(200, infos.length());
      he.getResponseBody().write(infos.getBytes());
      he.getResponseBody().close();
    }

    private void processCapture(HttpExchange he, int screen) throws AWTException, IOException {
      long time = System.currentTimeMillis();
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      if (screen < 0 || screen > ge.getScreenDevices().length - 1) {
        notFound(he);
        return;
      }
      time = System.currentTimeMillis();
      GraphicsDevice device = ge.getScreenDevices()[screen];
      int width = device.getDefaultConfiguration().getBounds().width;
      int height = device.getDefaultConfiguration().getBounds().height;
      Robot robot = new Robot(ge.getScreenDevices()[screen]);
      time = System.currentTimeMillis();
      BufferedImage desktop = robot.createScreenCapture(new Rectangle(width, height));
      time = System.currentTimeMillis();
      ByteArrayOutputStream image = new ByteArrayOutputStream(2048);
      time = System.currentTimeMillis();
      ImageIO.write(toCompatibleImage(desktop), "JPG", image);
      time = System.currentTimeMillis();
      he.getResponseHeaders().add("Content-Type", "image/jpeg");
      he.sendResponseHeaders(200, image.size());
      he.getResponseBody().write(image.toByteArray());
      he.getResponseBody().close();
    }

    private BufferedImage toCompatibleImage(BufferedImage image) {
      BufferedImage new_image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = (Graphics2D) new_image.getGraphics();
      g2d.drawImage(image, 0, 0, null);
      g2d.dispose();
      return new_image;
    }

    private void notFound(HttpExchange he) throws IOException {
      he.getResponseHeaders().add("Content-Type", "text/plain");
      he.sendResponseHeaders(404, err404.length());
      he.getResponseBody().write(err404.getBytes());
      he.getResponseBody().close();
    }

    private void notAllowed(HttpExchange he) throws IOException {
      he.getResponseHeaders().add("Content-Type", "text/plain");
      he.sendResponseHeaders(405, err405.length());
      he.getResponseBody().write(err405.getBytes());
      he.getResponseBody().close();
    }

    private void error(HttpExchange he, Exception e) throws IOException {
      he.getResponseHeaders().add("Content-Type", "text/plain");
      he.sendResponseHeaders(500, err500.length() + e.getMessage().length());
      he.getResponseBody().write(err500.getBytes());
      he.getResponseBody().write(e.getMessage().getBytes());
      he.getResponseBody().close();
    }

  }

}
