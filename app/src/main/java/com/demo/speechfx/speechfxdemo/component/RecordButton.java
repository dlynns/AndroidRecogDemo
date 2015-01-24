package com.demo.speechfx.speechfxdemo.component;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.activity.DemoActivity;
import com.demo.speechfx.speechfxdemo.enumeration.MicStatus;

import java.io.IOException;

/**
 * Created by Marlon on 1/24/2015.
 * RecordButton
 */
public class RecordButton extends Button {
  private static final String TAG = RecordButton.class.getSimpleName();
  private DemoActivity activity;
  private boolean startRecording = true;
  private MediaRecorder recorder = null;

  public RecordButton(Context activity) {
    super(activity);
    this.activity = (DemoActivity) activity;
    setText(activity.getString(R.string.start));
    setOnClickListener(clicker);
  }

  public RecordButton(Context activity, AttributeSet attrs) {
    super(activity, attrs);
    this.activity = (DemoActivity) activity;
    setText(activity.getString(R.string.start));
    setOnClickListener(clicker);
  }

  OnClickListener clicker = new OnClickListener() {
    public void onClick(View v) {
      onRecord(startRecording);

      if (startRecording) {
        setText(activity.getString(R.string.stop));
      } else {
        setText(activity.getString(R.string.start));
      }
      startRecording = !startRecording;
    }
  };

  public void onPause() {
    if (recorder != null) {
      recorder.release();
      recorder = null;
    }
  }

  public void onRecord(boolean start) {
    if (start) {
      startRecording();
    } else {
      stopRecording();
    }
  }

  private void startRecording() {
    activity.setStatus(MicStatus.GREEN);
    recorder = new MediaRecorder();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    recorder.setOutputFile(activity.fileName);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    try {
      recorder.prepare();
    } catch (IOException e) {
      Log.e(TAG, "prepare() failed");
    }
    recorder.start();
  }

  private void stopRecording() {
    activity.setStatus(MicStatus.RED);
    recorder.stop();
    recorder.release();
    recorder = null;
  }
}
