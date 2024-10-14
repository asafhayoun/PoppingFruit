package me.googroup.poppingfruit;

import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import me.googroup.popping_fruit.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button play = findViewById(R.id.play_button);
    Button settings = findViewById(R.id.settings_button);
    settings.setOnClickListener((view) -> {
      SettingsDialog dialog = new SettingsDialog(this);
      dialog.show();
    });
    play.setOnClickListener((view) -> {
      Intent intent = new Intent(MainActivity.this, GameActivity.class);
      startActivity(intent);
    });
  }
}