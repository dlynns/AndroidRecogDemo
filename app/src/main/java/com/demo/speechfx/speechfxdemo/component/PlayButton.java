package com.demo.speechfx.speechfxdemo.component;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.demo.speechfx.speechfxdemo.R;
import com.demo.speechfx.speechfxdemo.activity.DemoActivity;

import java.io.IOException;

/**
 * Created by Marlon on 1/24/2015.
 * PlayButton
 */
public class PlayButton extends Button {
  private static final String TAG = PlayButton.class.getSimpleName();
  private DemoActivity activity;
  private boolean startPlaying = true;
  private MediaPlayer player = null;


  public PlayButton(Context activity) {
    super(activity);
    this.activity = (DemoActivity) activity;
    setText(activity.getString(R.string.play));
    setOnClickListener(clicker);
  }

  public PlayButton(Context activity, AttributeSet attrs) {
    super(activity, attrs);
    this.activity = (DemoActivity) activity;
    setText(activity.getString(R.string.play));
    setOnClickListener(clicker);
  }

  OnClickListener clicker = new OnClickListener() {
    public void onClick(View v) {
      if (startPlaying) {
        setText(activity.getString(R.string.stop));
      } else {
        setText(activity.getString(R.string.play));
      }
      onPlay(startPlaying);
      startPlaying = !startPlaying;
    }
  };

  public void onPause() {
    if (player != null) {
      player.release();
      player = null;
    }
  }

  public void onPlay(boolean start) {
    if (start) {
      startPlaying();
    } else {
      stopPlaying();
    }
  }

  private void startPlaying() {
    player = new MediaPlayer();
    try {
      player.setDataSource(activity.fileName);
      player.prepare();
      player.start();
      player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
          activity.playButton.setText(activity.getString(R.string.play));
        }
      });
    } catch (IOException e) {
      Log.e(TAG, "prepare() failed");
    }
  }

  private void stopPlaying() {
    player.release();
    player = null;
  }

}
