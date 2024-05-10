package me.googroup.poppingfruit;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import me.googroup.poppingfruit.draw.Image;
import me.googroup.poppingfruit.draw.RenderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.util.List;

public class GameRenderer implements GLSurfaceView.Renderer {
  float widthFix, heightFix;
  Context context;
  GameRenderer(Context context) {
    this.context = context;
  }
  RenderProgram imageRenderer;
  Image background;
  Image cherryImage;
  Fruit lastFruit;

  long lastDrawTime;

  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    GLES20.glEnable(GLES20.GL_BLEND);
    //Uses to prevent transparent area to turn in black
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    // Set the background frame color
    GLES20.glClearColor(1.0f, 0.92f, 0.82f, 1.0f);
    imageRenderer = new RenderProgram(Image.vertexShader.load(), Image.fragmentShader.load());
    background = new Image(imageRenderer);
    background.setCoords(-1, -1, 2, 2);
    cherryImage = new Image(imageRenderer);
    try {
      background.setTexture(background.loadTexture(context, "game_background.jpeg"));
      cherryImage.setTexture(cherryImage.loadTexture(context, "cherry.png"));
    } catch (IOException e) {
      Log.e("RESOURCE", "Can't read resources");
    }
    lastFruit = new Fruit(Fruit.Type.CHERRY);
    Image.vertexShader.unload();
    Image.fragmentShader.unload();
    lastDrawTime = System.currentTimeMillis();
  }

  public void onDrawFrame(GL10 unused) {
    long lastTime = lastDrawTime;
    lastDrawTime = System.currentTimeMillis();
    float delta = (float) (lastDrawTime - lastTime);
    // Redraw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    background.draw();
    float cherryWidth = 0.125f * widthFix, cherryHeight = 0.125f * heightFix;
    Fruit thisFruit = lastFruit;
    while (thisFruit != null) {
      thisFruit.position.y -= delta * 0.0008f;
//      if(!CollisionDetector.circleFitsInRect(thisFruit.position.x + 1, thisFruit.position.y + 1,
//        0.25f, 2 * heightFix, 2 * widthFix)) {
      if(thisFruit.position.y < -1 + cherryHeight * 0.5f) {
        thisFruit.position.y = -1 + cherryHeight * 0.5f;
      }
      cherryImage.setCoords(thisFruit.position.x - cherryWidth * 0.5f, thisFruit.position.y - cherryHeight * 0.5f,
        cherryWidth, cherryHeight);
      cherryImage.draw();
      thisFruit = thisFruit.previousFruit;
    }
  }
  public void unload() {
    imageRenderer.unload();
    background.unloadTexture();
    cherryImage.unloadTexture();
  }
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    GLES20.glViewport(0, 0, width, height);
    float littler = (float) Math.max(width, height);
    widthFix = littler / (float)width;
    heightFix = littler / (float)height;
  }
}
