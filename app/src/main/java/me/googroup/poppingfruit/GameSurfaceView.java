package me.googroup.poppingfruit;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

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

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if(renderer.won) return false;
    int action = event.getActionMasked();
    if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
      int width = renderer.viewPortWidth, height = renderer.viewPortHeight;
      int x = (int)(width * (renderer.width-1)), y = (int)(height * (renderer.height-1));
      renderer.onMoveFinger(2 * renderer.width * event.getX() / width - renderer.width); // Game coordinates between -1 and 1
    } else if (action == MotionEvent.ACTION_UP) {
      renderer.onReleaseFinger();
    }
    return true;
  }
}
