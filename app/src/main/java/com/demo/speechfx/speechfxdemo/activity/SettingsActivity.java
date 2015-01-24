package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.SpeechFxApp;

/**
 * Created by Marlon on 1/19/2015.
 * public class SettingsActivity {
 */
public class SettingsActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.activity_settings);
    ((TextView) findViewById(R.id.recognitionSetting)).setText(SpeechFxApp.getInstance().getMode().getTitle());
    findViewById(R.id.regocnitionModeEdit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(SettingsActivity.this, RecognitionModeActivity.class);
        startActivity(intent);
      }
    });
    findViewById(R.id.noiseThresholdEdit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(SettingsActivity.this, NoiseThresholdActivity.class);
        startActivity(intent);
      }
    });
    findViewById(R.id.maxRecordingTimeEdit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(SettingsActivity.this, MaxRecordTimeActivity.class);
        startActivity(intent);
      }
    });
    ((TextView) findViewById(R.id.noiseThresholdValue)).setText(String.valueOf(SpeechFxApp.getInstance().getNoiseThreshold()));
    ((TextView) findViewById(R.id.maxRecordingTimeValue)).setText(String.valueOf(SpeechFxApp.getInstance().getMaxRecordTime()));
  }
}
