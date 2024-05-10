package me.googroup.poppingfruit;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameSurfaceView extends GLSurfaceView {
  private final GameRenderer renderer;
  public GameSurfaceView(Context context) {
    super(context);
    // Create an OpenGL ES 2.0 context
    setEGLContextClientVersion(2);
    renderer = new GameRenderer(context);
    // Set the Renderer for drawing on the GLSurfaceView
    setRenderer(renderer);
  }

}
