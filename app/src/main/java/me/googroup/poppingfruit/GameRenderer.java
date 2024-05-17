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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameRenderer implements GLSurfaceView.Renderer {
  float widthFix, heightFix;
  Context context;
  GameRenderer(Context context) {
    this.context = context;
  }
  RenderProgram imageRenderer, victoryRenderer;
  int victoryEffectHandle;
  float victoryEffectCoefficient = 0;
  Image background;
  Image victoryImage;
  boolean won = false;
  Image[] fruitImages = new Image[2];
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
    background.setCoords(-1, -1, 2, 2);
    victoryImage = new Image(victoryRenderer);
    for(int i = 0; i < fruitImages.length; ++i) {
      fruitImages[i] = new Image(imageRenderer);
    }
    try {
      background.setTexture(background.loadTexture(context, "game_background.jpeg"));
      victoryImage.loadTexture(context, "victory.png");
      fruitImages[0].loadTexture(context, "cherry.png");
      fruitImages[1].loadTexture(context, "watermelon.png");
    } catch (IOException e) {
      Log.e("RESOURCE", "Can't read resources");
    }
    fruits = new ArrayList<Fruit>(Arrays.asList(
      new Fruit(0, 0, Fruit.Type.CHERRY),
      new Fruit(0, 0.3, Fruit.Type.CHERRY),
      new Fruit(0, 0.6, Fruit.Type.CHERRY),
      new Fruit(0, -0.3, Fruit.Type.CHERRY)));
    Image.vertexShader.unload();
    Image.fragmentShader.unload();
    lastDrawTime = System.nanoTime();
    won = false;
    victoryEffectCoefficient = 0;
  }

  public void onDrawFrame(GL10 unused) {
    long lastTime = lastDrawTime;
    lastDrawTime = System.nanoTime();
    float delta = (float) (lastDrawTime - lastTime) * 0.000001f;
    // Redraw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    background.draw();
    if(won) {
      victoryEffectCoefficient += delta * 0.001f;
      victoryImage.setCoords(-0.75f, 0f, 1.5f, 0.75f);
      GLES20.glUseProgram(victoryRenderer.id);
      victoryEffectCoefficient %= 3;
      GLES20.glUniform1f(victoryEffectHandle, victoryEffectCoefficient > 1 ? 1 - (victoryEffectCoefficient-1)*0.5f : victoryEffectCoefficient);
      victoryImage.draw(true);
    } else {
      for (int i = 0; i < fruits.size(); i++) {
        Fruit fruit = fruits.get(i);
        if (fruit == null) continue;
        fruit.y -= delta * 0.0005;
        double radius = fruit.radius();
        if (fruit.y < -1.0 + radius) {
          fruit.y = -1.0 + radius;
        }
        for (int j = i + 1; j < fruits.size(); j++) {
          if (fruits.get(j) == null) continue;
          if (fruits.get(j).type == fruit.type && fruits.get(j).intersects(fruit)) {
            Fruit otherFruit = fruits.get(j);
            System.out.println("Collision between fruit " + i + " and fruit " + j);
            fruits.set(i, null);
            fruits.set(j, null);
            Fruit.Type next = fruit.type.nextFruit();
            if (next == null) {
              won = true;
              return;
            }
            Fruit newFruit = new Fruit((fruit.x + otherFruit.x) * 0.5, (fruit.y + otherFruit.y) * 0.5, next);
            fruits.add(newFruit);
            break;
          }
        }
      }
    }
    for (int i = 0; i < fruits.size(); i++) {
      if (fruits.get(i) == null) {
        fruits.remove(i);
        i--;
      }
    }
    for(Fruit fruit : fruits) {
      Image image = fruitImages[fruit.type.ordinal()];
      float fruitWidth = (float)fruit.radius() * 2f * widthFix, fruitHeight = (float)fruit.radius() * 2f * heightFix;
      image.setCoords((float)fruit.x - fruitWidth * 0.5f, (float)fruit.y - fruitHeight * 0.5f,
        fruitWidth, fruitHeight);
      image.draw();
    }
  }
  public void unload() {
    imageRenderer.unload();
    background.unloadTexture();
    for(Image img : fruitImages) img.unloadTexture();
    victoryImage.unloadTexture();
  }
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    GLES20.glViewport(0, 0, width, height);
    float littler = (float) Math.max(width, height);
    widthFix = littler / (float)width;
    heightFix = littler / (float)height;
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
