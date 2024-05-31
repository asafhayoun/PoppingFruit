package me.googroup.poppingfruit;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import me.googroup.poppingfruit.draw.Image;
import me.googroup.poppingfruit.draw.RenderProgram;
import me.googroup.poppingfruit.draw.Shader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameRenderer implements GLSurfaceView.Renderer {
  Context context;
  GameRenderer(Context context) {
    this.context = context;
  }
  RenderProgram imageRenderer, victoryRenderer;
  int victoryEffectHandle;
  float victoryEffectCoefficient = 0;
  Image background;
  Image victoryImage;
  boolean won = false, lost = false;
  Image[] fruitImages = new Image[4];
  List<Fruit> fruits;

  long lastDrawTime;

  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    GLES20.glEnable(GLES20.GL_BLEND);
    //Uses to prevent transparent area to turn in black
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    // Set the background frame color
    GLES20.glClearColor(1.0f, 0.92f, 0.82f, 1.0f);
    imageRenderer = new RenderProgram(Image.vertexShader.load(), Image.fragmentShader.load());
    victoryRenderer = new RenderProgram(Image.vertexShader, victoryShader.load());
    victoryEffectHandle = GLES20.glGetUniformLocation(victoryRenderer.id, "vEffect");


    background = new Image(imageRenderer);
    victoryImage = new Image(victoryRenderer);
    for(int i = 0; i < fruitImages.length; ++i) {
      fruitImages[i] = new Image(imageRenderer);
    }
    try {
      background.loadTexture(context, "game_background.jpeg");
      victoryImage.loadTexture(context, "victory.png");
      fruitImages[0].loadTexture(context, "blueberry.png");
      fruitImages[1].loadTexture(context, "cherry.png");
      fruitImages[2].loadTexture(context, "clementine.png");
      fruitImages[3].loadTexture(context, "watermelon.png");
    } catch (IOException e) {
      Log.e("RESOURCE", "Can't read resources");
    }
//    fruits = new ArrayList<Fruit>(Arrays.asList(
//      new Fruit(0, 0, Fruit.Type.CHERRY),
//      new Fruit(0, 0.3, Fruit.Type.CHERRY),
//      new Fruit(0, 0.6, Fruit.Type.CHERRY),
//      new Fruit(0, -0.3, Fruit.Type.CHERRY)));
    fruits = new ArrayList<>(128);
    Image.vertexShader.unload();
    Image.fragmentShader.unload();
    victoryShader.unload();
    lastDrawTime = System.nanoTime();
    won = false;
    victoryEffectCoefficient = 0;
  }
  void onMoveFinger(float x) {
    fruitToAdd.x = x;
  }
  long lastReleasedFruit = 0;
  Random rng = new Random();
  Fruit fruitToAdd = new Fruit(0, 0.75, Fruit.Type.BLUEBERRY);
  synchronized boolean onReleaseFinger() {
    long now = System.currentTimeMillis();
    if(now < lastReleasedFruit + 667) return false;
    lastReleasedFruit = now;
    for (Fruit f : fruits) {
      if(f != null && f.intersects(fruitToAdd)) {
        return true; // LOST! :(
      }
    }
    fruits.add(fruitToAdd);
    fruitToAdd = new Fruit(fruitToAdd.x, 0.75, Fruit.Type.BLUEBERRY);
    if(rng.nextDouble() < 0.5) {
      if(rng.nextDouble() < 0.5) {
        fruitToAdd.type = Fruit.Type.CLEMENTINE;
      } else {
        fruitToAdd.type = Fruit.Type.CHERRY;
      }
    }
    return false;
  }
  public static final double GAME_SPEED = 0.0008;
  public synchronized void onDrawFrame(GL10 unused) {
    long lastTime = lastDrawTime;
    lastDrawTime = System.nanoTime();
    float delta = Math.min(50f, (float) (lastDrawTime - lastTime) * 0.000001f);
    // Redraw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    background.setCoords(-1 * width, -1 * height , 2 * width, 2 * height);
    background.draw();
    float newFruitSize = (float)Fruit.radii[fruitToAdd.type.ordinal()];
    fruitImages[fruitToAdd.type.ordinal()].setCoords((float) (fruitToAdd.x - newFruitSize), 0.75f - newFruitSize,
      newFruitSize * 2 , newFruitSize * 2 ).draw();
    if(won) {
      victoryEffectCoefficient += delta * (float)GAME_SPEED;
      victoryEffectCoefficient %= 3;
      victoryImage.setCoords(-0.75f * width, 0f * height, 1.5f * width, 0.75f * height);
      GLES20.glUseProgram(victoryRenderer.id);
      GLES20.glUniform1f(victoryEffectHandle, victoryEffectCoefficient > 1 ? 1 - (victoryEffectCoefficient-1)*0.5f : victoryEffectCoefficient);
      victoryImage.draw(true);
    } else {
      int fruitAmount = fruits.size();
      int currentAddedFruitIdx = 0;
      Fruit[] addedFruits = new Fruit[fruitAmount];
      mainLoop: for (int i = 0; i < fruitAmount; i++) {
        Fruit fruit = fruits.get(i);
        if (fruit == null) continue;
        // Calculate the net response considering all contact points
//        float heightDecrease = 1;
//        double netDx = 0, netDy = 0;
        int numContacts = 0;
        // Save the original position
        double originalX = fruit.lastX = fruit.x;
        double originalY = fruit.lastY = fruit.y;
        double originalRotation = fruit.rotation;
        for (int j = 0; j < fruitAmount; j++) {
          if (i == j || fruits.get(j) == null) continue;
          if(!fruits.get(j).intersects(fruit)) continue;
          Fruit otherFruit = fruits.get(j);
          if (otherFruit.type == fruit.type) {
            System.out.println("Collision between fruit " + i + " and fruit " + j);
            fruits.set(i, null);
            fruits.set(j, null);
            Fruit.Type next = fruit.type.nextFruit();
            if (next == null) {
              won = true;
              break mainLoop;
            }
            Fruit higherFruit = fruit.y > otherFruit.y ? fruit : otherFruit;
            Fruit newFruit = new Fruit((fruit.x + otherFruit.x) * 0.5, (fruit.y + otherFruit.y) * 0.5, next);
//            Fruit newFruit = new Fruit(higherFruit.x, higherFruit.y, next);
            fruit = higherFruit;

            addedFruits[currentAddedFruitIdx++] = newFruit;
            break;
          } else {
            if(fruit.y > otherFruit.y) {
//              double dx = otherFruit.x - fruit.x;
//              double dy = otherFruit.y - fruit.y;
//              double dist = Math.sqrt(dx * dx + dy * dy);
//              double contactX = fruit.x + (dx / dist) * fruit.radius();
//              double contactY = fruit.y + (dy / dist) * fruit.radius();
              double dx = fruit.x - otherFruit.x;
              double dy = fruit.y - otherFruit.y;
//              double dist = Math.sqrt(dx * dx + dy * dy);
//              double overlap = (otherFruit.radius() + fruit.radius()) - dist;

              //if (overlap > 0) {
                double angle = Math.atan2(dy, dx);// - GAME_SPEED * Math.PI * delta * (fruit.x - otherFruit.x) / otherFruit.radius();
                //double angle = Math.PI/2 - ((fruit.x - otherFruit.x) /
                  //((fruit.radius() + otherFruit.radius()) / otherFruit.radius()) / otherFruit.radius() * Math.PI * 0.5);
                //double correctionX = Math.cos(angle) * overlap;
                //double correctionY = Math.sin(angle) * overlap;
                //netDx += correctionX;
                //netDy += correctionY;
              //}
              numContacts++;
              //double angle = Math.PI/2 - ((fruit.x - otherFruit.x) /
              //  ((fruit.radius() + otherFruit.radius()) / otherFruit.radius()) / otherFruit.radius() * Math.PI * 0.5);
//              double angle = Math.acos((fruit.x - otherFruit.x) / (otherFruit.radius() + fruit.radius()));
//              double nerf = Math.abs(Math.cos(angle));
             // fruit.x += Math.sin(angle) * otherFruit.radius();
//              Log.d("MOVEMENT", String.valueOf(angle * 180 / Math.PI));
              //heightDecrease -= 1;
              // double oldX = fruit.x, oldY = fruit.y;
//              if(previousRotationRadius != 0) {
//                //double newSupposedX = otherFruit.x + Math.cos(angle - GAME_SPEED * Math.PI * delta * (fruit.x - otherFruit.x) / otherFruit.radius()) * (otherFruit.radius() + fruit.radius());
//              } else {
//              } else {

              if(numContacts == 1) {
                double otherX = j < i ? otherFruit.lastX : otherFruit.x;
                double otherY = j < i ? otherFruit.lastY : otherFruit.y;
                fruit.x = otherX + Math.cos(angle - GAME_SPEED * Math.PI * delta * (fruit.x - otherFruit.x) / otherFruit.radius()) * (otherFruit.radius() + fruit.radius());
                fruit.y = otherY + Math.sin(angle - GAME_SPEED * Math.PI * delta * (fruit.x - otherFruit.x) / otherFruit.radius()) * (otherFruit.radius() + fruit.radius());
                fruit.rotation -= GAME_SPEED * Math.PI * 4 * delta / otherFruit.radius() * (fruit.x - otherFruit.x);
              }
            }
          }
        }
//        if(numContacts > 0) {
//          fruit.x += netDx / numContacts;
//          Log.d("NET", netDx + ", " + netDy);
//          fruit.y += netDy / numContacts;
//        }
        if(numContacts > 1) {
          fruit.x = originalX;
          fruit.y = originalY;
          fruit.rotation = originalRotation;
        }
        double radius = fruit.radius();


        // Check and adjust x position
//        if (fruit.x < -1*width + radius) {
//          fruit.x = -1*width + radius;
//        }
//        if (fruit.x > 1*width - radius) {
//          fruit.x = 1*width - radius;
//        }
//
//
//        // Check for collisions after x adjustment
//        for (Fruit c2 : fruits) {
//          if (c2 != null && fruit.y > c2.y && c2.intersects(fruit)) {
//            fruit.x = originalX; // Revert x adjustment if it causes collision
//            break;
//          }
//        }
//
        if(numContacts == 0)
          fruit.y -= delta * GAME_SPEED;
        // Check and adjust y position
        if (fruit.x < -1*width + radius) {
          fruit.y = originalY;
          fruit.x = -width + radius;
          fruit.rotation = originalRotation;
        }
        if (fruit.x > 1*width - radius) {
          fruit.y = originalY;
          fruit.x = 1*width - radius;
          fruit.rotation = originalRotation;
        }
        if(fruit.y < -1*height + radius) {
          fruit.y = -height + radius;
          fruit.x = originalX;
          fruit.rotation = originalRotation;
        }
//
//        // Check for collisions after y adjustment
//        for (Fruit c2 : fruits) {
//          if (c2 != null && fruit.y > c2.y && c2.intersects(fruit)) {
//            fruit.y = originalY; // Revert x adjustment if it causes collision
//            break;
//          }
//        }

      }
      if(currentAddedFruitIdx > 0)
        fruits = Stream.concat(
          fruits.stream().filter(Objects::nonNull), Arrays.stream(addedFruits)).collect(Collectors.toList());
    }
    for(Fruit fruit : fruits) {
      if(fruit == null) continue;
      Image image = fruitImages[fruit.type.ordinal()];
      float fruitWidth = (float)fruit.radius() * 2f, fruitHeight = (float)fruit.radius() * 2f;
      image.setCoords((float)fruit.x - fruitWidth * 0.5f, (float)fruit.y - fruitHeight * 0.5f,
        fruitWidth, fruitHeight, (float)fruit.rotation);
      image.draw();
    }

  }
  public void unload() {
    imageRenderer.unload();
    background.unloadTexture();
    for(Image img : fruitImages) img.unloadTexture();
    victoryImage.unloadTexture();
  }
  public int viewPortWidth, viewPortHeight;
  float width, height;
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    int bigger = Math.max(width, height);
//    widthFix = (float)bigger / (float)width;
//    heightFix = (float)bigger / (float)height
    this.width =  (float)width / (float)bigger;
    this.height = (float)height / (float)bigger;
    // I spent 2 hours on this, please do not touch
    GLES20.glViewport((int)((-1 + this.width) * bigger * 0.5),
      (int)((-1 + this.height) * bigger * 0.5), bigger, bigger);
    //float i = (-1 - this.width) * -bigger;
    Log.i("VIEWPORT", "Width: " + this.width + " Height: " + this.height);
    viewPortWidth = width;
    viewPortHeight = height;
  }
  public static final Shader victoryShader = new Shader(GLES20.GL_FRAGMENT_SHADER,
    "\n" +
      "precision mediump float;" +
      "uniform sampler2D u_Texture;" +
      "uniform float vEffect;" +
      "varying vec2 v_TexCoord;" +
      "void main() {" +
      "  vec4 color = texture2D(u_Texture, v_TexCoord);" +
      "  gl_FragColor = vec4(color.r,color.g,color.b,1.0 - (color.b * vEffect));" +
      "}"
  );
}
