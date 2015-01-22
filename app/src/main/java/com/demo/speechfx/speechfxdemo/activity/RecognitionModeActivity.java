package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.enumeration.RecognitionMode;
import com.demo.speechfx.speechfxdemo.SpeechFxApp;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by Marlon on 1/15/2015.
 * SettingsActivity
 */
public class RecognitionModeActivity extends Activity {
  private boolean scrolling = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.recognition_mode_settings);

    final String modeChoices[] = RecognitionMode.getTitles();
    final WheelView modeWheel = (WheelView) findViewById(R.id.modeSelection);
    modeWheel.setVisibleItems(15);

    int current = SpeechFxApp.getInstance().getMode().ordinal();
    modeWheel.setCurrentItem(current);
    modeWheel.setViewAdapter(new TextWheelAdapter(this, modeChoices, current));
    modeWheel.addChangingListener(new OnWheelChangedListener() {
      @Override
      public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (!scrolling) {
          updateMode(modeWheel, modeChoices, newValue);
        }
      }
    });
    modeWheel.addScrollingListener(new OnWheelScrollListener() {
      @Override
      public void onScrollingStarted(WheelView wheel) {
        scrolling = true;
      }
      @Override
      public void onScrollingFinished(WheelView wheel) {
        scrolling = false;
        updateMode(modeWheel, modeChoices, wheel.getCurrentItem());
      }
    });
  }

  private void updateMode(WheelView mode, String choices[], int newValue) {
    SpeechFxApp.getInstance().setMode(RecognitionMode.values()[newValue]);
    TextWheelAdapter adapter = new TextWheelAdapter(this, choices, newValue);
    adapter.setTextSize(18);
    mode.setViewAdapter(adapter);
    mode.setCurrentItem(newValue);
  }

  public void saveSettings(View view) {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
    finish();
  }

  private class TextWheelAdapter extends ArrayWheelAdapter<String> {
    int currentItem;
    int currentValue;

    public TextWheelAdapter(Context context, String[] choices, int current) {
      super(context, choices);
      this.currentItem = current;
      setTextSize(16);
    }

    @Override
    protected void configureTextView(TextView view) {
      super.configureTextView(view);
      if (currentItem == currentValue) {
        view.setTextColor(0xFF0000F0);
      }
      view.setTypeface(Typeface.SANS_SERIF);
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
      currentItem = index;
      return super.getItem(index, cachedView, parent);
    }
  }
}
