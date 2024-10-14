package me.googroup.poppingfruit;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import android.os.Bundle;
import me.googroup.popping_fruit.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends Dialog {

  SharedPreferences settingsPref;

  public SettingsDialog(@NonNull Context context) {
    super(context, R.style.Dialog);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_settings);
    setTitle("Settings");
//    if (getSupportActionBar() != null) {
//      getSupportActionBar().setTitle("Settings");
//      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//      getSupportActionBar().setDisplayShowHomeEnabled(true);
//    }
    // setSupportActionBar(toolbar);
    SeekBar music = findViewById(R.id.music_slider), sfx = findViewById(R.id.sfx_slider);
    settingsPref = getContext().getSharedPreferences("settings", MODE_PRIVATE);
    try {
      music.setProgress(settingsPref.getInt("music" , 100));
    } catch (Exception e){
      music.setProgress(100);
    }
    try {
      sfx.setProgress(settingsPref.getInt("sfx" , 100));
    } catch (Exception e){
      sfx.setProgress(100);
    }
    music.setOnSeekBarChangeListener(new UpdateSetting("music"));
    sfx.setOnSeekBarChangeListener(new UpdateSetting("sfx"));
  }
  class UpdateSetting implements SeekBar.OnSeekBarChangeListener {
    public UpdateSetting(String name) {
      settingName = name;
    }
    String settingName;
    boolean update;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      update = true;
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      if(update)
        settingsPref.edit().putInt(settingName, seekBar.getProgress()).apply();
      update = false;
    }
  }
//  @Override
//  public boolean onSupportNavigateUp() {
//    getOnBackPressedDispatcher().onBackPressed();
//    return true;
//  }
}