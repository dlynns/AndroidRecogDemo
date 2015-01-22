package com.demo.speechfx.speechfxdemo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by Marlon on 1/17/2015.
 * TextWheelAdapter
 */
public class TextWheelAdapter extends ArrayWheelAdapter<String> {
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
