package com.demo.speechfx.speechfxdemo;

import android.app.Application;
import android.content.Context;

import com.demo.speechfx.speechfxdemo.enumeration.MicStatus;
import com.demo.speechfx.speechfxdemo.enumeration.RecognitionMode;

/**
 * Created by Marlon on 1/15/2015.
 * SpeechFxApp
 */
public class SpeechFxApp extends Application {
  public static final int MAX_RECORD_TIME_INTERVAL = 120000;
  public static final int NOISE_THRESHOD_INTERVAL = 100;
  private static Context appContext;
  private static SpeechFxApp singleton;
  private RecognitionMode mode = RecognitionMode.SINGLE;
  private int noiseThreshold = NOISE_THRESHOD_INTERVAL * 10 / 2;
  private int maxRecordTime = MAX_RECORD_TIME_INTERVAL * 10 / 2;
  private MicStatus micStatus = MicStatus.GRAY;


  public void init(Context context) {
    if (appContext == null) {
      appContext = context;
    }
  }

  private Context getContext() {
    return appContext;
  }

  public static Context get() {
    return getInstance().getContext();
  }

  public static SpeechFxApp getInstance() {
    return singleton == null ? (singleton = new SpeechFxApp()) : singleton;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    singleton = this;
  }

  public RecognitionMode getMode() {
    return mode;
  }

  public void setMode(RecognitionMode mode) {
    this.mode = mode;
  }

  public int getNoiseThreshold() {
    return noiseThreshold;
  }

  public void setNoiseThreshold(int noiseThreshold) {
    this.noiseThreshold = noiseThreshold;
  }

  public int getMaxRecordTime() {
    return maxRecordTime;
  }

  public void setMaxRecordTime(int maxRecordTime) {
    this.maxRecordTime = maxRecordTime;
  }

  public MicStatus getMicStatus() {
    return micStatus;
  }

  public void setMicStatus(MicStatus micStatus) {
    this.micStatus = micStatus;
  }
}
