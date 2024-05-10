package me.googroup.poppingfruit;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import me.googroup.popping_fruit.R;

public class GameActivity extends AppCompatActivity {
  GLSurfaceView view;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    view = new GameSurfaceView(this);
    setContentView(view);
  }
}