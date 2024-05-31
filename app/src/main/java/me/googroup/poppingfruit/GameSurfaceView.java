package me.googroup.poppingfruit;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
      if(renderer.onReleaseFinger()) {
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("You have lost the game")
          .setPositiveButton("OK", (dialog, id) -> {
            // Action to perform when OK is clicked
            dialog.dismiss(); // Dismiss the dialog
            Context context = getContext();
            if (context instanceof AppCompatActivity) {
              ((AppCompatActivity)context).finish();
            }

          });
        AlertDialog dialog = builder.create();
        dialog.show();
      }
    }
    return true;
  }
}
