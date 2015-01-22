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
import com.demo.speechfx.speechfxdemo.SpeechFxApp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by Marlon on 1/19/2015.
 * NoiseThresholdActivity
 */
public class NoiseThresholdActivity extends Activity {
  private boolean scrolling = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.noise_threshold_setting);

    int current = SpeechFxApp.getInstance().getNoiseThreshold() - 1;
    final String noiseThresholds[] = getList();
    final WheelView thresholdWheel = (WheelView) findViewById(R.id.thresholdSelection);
    thresholdWheel.setCurrentItem(current);
    thresholdWheel.setVisibleItems(15);
    thresholdWheel.setViewAdapter(new TextWheelAdapter(this, noiseThresholds, current));
    thresholdWheel.addChangingListener(new OnWheelChangedListener() {
      @Override
      public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (!scrolling) {
          updateMode(thresholdWheel, noiseThresholds, newValue);
        }
      }
    });
    thresholdWheel.addScrollingListener(new OnWheelScrollListener() {
      @Override
      public void onScrollingStarted(WheelView wheel) {
        scrolling = true;
      }

      @Override
      public void onScrollingFinished(WheelView wheel) {
        scrolling = false;
        updateMode(thresholdWheel, noiseThresholds, wheel.getCurrentItem());
      }
    });
  }

  private void updateMode(WheelView mode, String choices[], int newValue) {
    SpeechFxApp.getInstance().setNoiseThreshold(newValue * SpeechFxApp.NOISE_THRESHOD_INTERVAL);
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

  public String[] getList() {
    List<String> list = new LinkedList<>();
    for (int i = 0; i <= 10; i++) {
      list.add(String.valueOf(i * SpeechFxApp.NOISE_THRESHOD_INTERVAL));
    }
    return list.toArray(new String[list.size()]);
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

