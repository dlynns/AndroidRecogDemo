package com.demo.speechfx.speechfxdemo.enumeration;

/**
 * Created by Marlon on 1/15/2015.
 * RecognitionMode
 */
public enum RecognitionMode {
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

  public String getTitle() {
    return title;
  }
}
