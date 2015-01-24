package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.SpeechFxApp;
import com.demo.speechfx.speechfxdemo.enumeration.MicStatus;

public class DemoActivity extends Activity implements View.OnClickListener {
  TextView recognitionModeValue;
  TextView scoreValue;
  Button startButton;
  Button playButton;
  ImageView micLight;
  ImageView backArrow;
  TextView helpButton;
  TextView settingsButton;
  ImageView forwardArrow;

  MicStatus micStatus = MicStatus.GRAY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    setContentView(R.layout.activity_demo);
    SpeechFxApp.getInstance().init(getApplicationContext());

    recognitionModeValue = (TextView) findViewById(R.id.recognitionModeValue);
    scoreValue = (TextView) findViewById(R.id.scoreValue);

    startButton = (Button) findViewById(R.id.startButton);
    startButton.setOnClickListener(this);
    playButton = (Button) findViewById(R.id.playButton);
    playButton.setOnClickListener(this);
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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_demo, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.fonixVoice:
        doFonix();
        break;
      case R.id.startButton:
        start();
        break;
      case R.id.playButton:
        play();
        break;
      case R.id.backArrow:
      case R.id.helpButton:
        help();
        break;
      case R.id.settingsButton:
      case R.id.forwardArrow:
        settings();
        break;
    }
  }

  private void start() {
    micStatus = MicStatus.GREEN;
    micLight.setImageResource(micStatus.getResource());
  }

  private void play() {
    micStatus = MicStatus.RED;
    micLight.setImageResource(micStatus.getResource());
  }

  private void help() {

  }

  private void doFonix() {
    Intent intent = new Intent(this, FonixAsrSimpleExample.class);
    startActivity(intent);
  }

  private void settings() {
    Intent intent = new Intent(this, SettingsActivity.class);
//    Bundle bundle = new Bundle();
//    bundle.putSerializable(RECOGNITION_MODE_KEY, mode);
//    intent.putExtras(bundle);
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
