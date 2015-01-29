package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.SpeechFxApp;
import com.demo.speechfx.speechfxdemo.component.PlayButton;
import com.demo.speechfx.speechfxdemo.component.RecordButton;
import com.demo.speechfx.speechfxdemo.enumeration.MicStatus;

import java.io.IOException;

public class DemoActivity extends Activity implements View.OnClickListener {
  private static final String TAG = DemoActivity.class.getSimpleName();
  private TextView recognitionModeValue;
  private TextView scoreValue;
  private RecordButton startButton;
  public PlayButton playButton;
  private ImageView micLight;
  private ImageView backArrow;
  private TextView helpButton;
  private TextView settingsButton;
  private ImageView forwardArrow;

  private MicStatus micStatus = MicStatus.GRAY;

  public final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    setContentView(R.layout.activity_demo);
    SpeechFxApp.getInstance().init(getApplicationContext());

    recognitionModeValue = (TextView) findViewById(R.id.recognitionModeValue);
    scoreValue = (TextView) findViewById(R.id.scoreValue);

    startButton = (RecordButton) findViewById(R.id.startButton);
    playButton = (PlayButton) findViewById(R.id.playButton);
    micLight = (ImageView) findViewById(R.id.mic_light);

    OpenBrowser openBrowser = new OpenBrowser("http:/www.speechfxinc.com");
    backArrow = (ImageView) findViewById(R.id.backArrow);
    backArrow.setOnClickListener(this);
    backArrow.setOnClickListener(openBrowser);

    helpButton = (TextView) findViewById(R.id.helpButton);
    helpButton.setOnClickListener(this);
    helpButton.setOnClickListener(openBrowser);

    settingsButton = (TextView) findViewById(R.id.settingsButton);
    settingsButton.setOnClickListener(this);
    forwardArrow = (ImageView) findViewById(R.id.forwardArrow);
    forwardArrow.setOnClickListener(this);

    recognitionModeValue.setText(SpeechFxApp.getInstance().getMode().getTitle());
    micStatus = SpeechFxApp.getInstance().getMicStatus();
    micLight.setImageResource(micStatus.getResource());

    findViewById(R.id.fonixVoice).setOnClickListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    startButton.onPause();
    playButton.onPause();
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.fonixVoice:
        doFonix();
        break;
      case R.id.settingsButton:
      case R.id.forwardArrow:
        settings();
        break;
    }
  }

  public void setStatus(MicStatus status) {
    micStatus = status;
    micLight.setImageResource(micStatus.getResource());
  }

  private void doFonix() {
    Intent intent = new Intent(this, AudioRecordActivity.class);
    startActivity(intent);
  }

  private void settings() {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
  }

  class OpenBrowser implements View.OnClickListener {
    private final String url;

    OpenBrowser(String url) {
      this.url = url;
    }

    @Override
    public void onClick(View v) {
      DemoActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
  }
}
