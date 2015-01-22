package com.demo.speechfx.speechfxdemo.enumeration;

import com.demo.speechfx.speechfxdemo.Titled;

/**
 * Created by Marlon on 1/15/2015.
 * RecognitionMode
 */
public enum RecognitionMode implements Titled {
  SINGLE("Single"), MULTIPLE("Multiple");

  String title;

  RecognitionMode(String title) {
    this.title = title;
  }

  public static String[] getTitles() {
    String[] titles = new String[values().length];
    int i = 0;
    for (RecognitionMode mode : values()) {
      titles[i++] = mode.getTitle();
    }
    return titles;
  }

  @Override
  public String getTitle() {
    return title;
  }
}
