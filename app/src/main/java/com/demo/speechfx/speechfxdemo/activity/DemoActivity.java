package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.SpeechFxApp;
import com.demo.speechfx.speechfxdemo.component.PlayButton;
import com.demo.speechfx.speechfxdemo.component.RecordButton;
import com.demo.speechfx.speechfxdemo.enumeration.MicStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoActivity extends Activity implements View.OnClickListener {
  private static final String TAG = DemoActivity.class.getSimpleName();
  private TextView recognitionModeValue;
  private TextView scoreValue;
  private RecordButton startButton;
  public PlayButton playButton;
  private ImageView micLight;
  private ImageView addWord;
  private ImageView backArrow;
  private TextView helpButton;
  private TextView settingsButton;
  private ImageView forwardArrow;
  private List<String> wordList = new ArrayList<String>();

  private MicStatus micStatus = MicStatus.GRAY;

  public final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    wordList.add("one");
    wordList.add("two");
    wordList.add("three");
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

    addWord = (ImageView) findViewById(R.id.addWord);

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

  public void onClickAddWord(View view) {
    final EditText editText = new EditText(this);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.add_word) + " " + wordList.size());
    builder.setMessage(R.string.add_word);
    builder.setView(editText);
    builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        if (!editText.getText().toString().trim().equals("")) {
          wordList.add(editText.getText().toString().trim());
          final WordArrayAdapter adapter = new WordArrayAdapter(DemoActivity.this, R.layout.single_word_layout, wordList);
          ((ListView) findViewById(R.id.wordList)).setAdapter(adapter);
        }
      }
    });
    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.dismiss();
      }
    });
    builder.create().show();
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

  private class WordArrayAdapter extends ArrayAdapter<String> {
    HashMap<String, Integer> idMap = new HashMap<String, Integer>();

    public WordArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
      super(context, textViewResourceId, objects);
      for (int i = 0; i < objects.size(); ++i) {
        idMap.put(objects.get(i), i);
      }
    }

    @Override
    public long getItemId(int position) {
      String item = getItem(position);
      return idMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }
  }
}
