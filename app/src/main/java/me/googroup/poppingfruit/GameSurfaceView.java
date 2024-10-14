package me.googroup.poppingfruit;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.googroup.popping_fruit.R;

public class GameSurfaceView extends GLSurfaceView {
  public final GameRenderer renderer;
  public GameSurfaceView(Context context) {
    super(context);
    // Create an OpenGL ES 2.0 context
    setEGLContextClientVersion(2);
    SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    renderer = new GameRenderer(context);
    renderer.popSound = MediaPlayer.create(context, R.raw.pop);
    renderer.music = MediaPlayer.create(context, R.raw.music);
    renderer.sfxVol = sharedPreferences.getInt("sfx", 100) * 0.01f;
    renderer.musicVol = sharedPreferences.getInt("music", 100) * 0.01f;
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
