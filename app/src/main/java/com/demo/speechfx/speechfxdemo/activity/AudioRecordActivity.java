package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.demo.speechfx.speechfxdemo.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecordActivity extends Activity {
  private static final String TAG = AudioRecordActivity.class.getSimpleName();
  private static final int RECORDER_SAMPLE_RATE = 8000;
  private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
  private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private static final int BUFFER_ELEMENTS_2_REC = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
  private static final int BYTES_PER_ELEMENT = 2; // 2 bytes in 16bit format
  private AudioRecord recorder = null;
  private Thread recordingThread = null;
  private boolean isRecording = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.some_layout);
    setButtonHandlers();
    enableButtons(false);
//    int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
  }

  private void setButtonHandlers() {
    findViewById(R.id.startButton).setOnClickListener(btnClick);
    findViewById(R.id.playButton).setOnClickListener(btnClick);
  }

  private void enableButton(int id, boolean isEnable) {
    findViewById(id).setEnabled(isEnable);
  }

  private void enableButtons(boolean isRecording) {
    enableButton(R.id.startButton, !isRecording);
    enableButton(R.id.playButton, isRecording);
  }

  private void startRecording() {
    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, BUFFER_ELEMENTS_2_REC * BYTES_PER_ELEMENT);
    recorder.startRecording();
    isRecording = true;
    recordingThread = new Thread(new Runnable() {
      public void run() {
        writeAudioDataToFile();
      }
    }, "AudioRecorder Thread");
    recordingThread.start();
  }

  //convert short to byte
  private byte[] short2byte(short[] sData) {
    int shortArraySize = sData.length;
    byte[] bytes = new byte[shortArraySize * 2];
    for (int i = 0; i < shortArraySize; i++) {
      bytes[i * 2] = (byte) (sData[i] & 0x00FF);
      bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
      sData[i] = 0;
    }
    return bytes;
  }

  private void writeAudioDataToFile() {
    // Write the output audio in byte
    String filePath = Environment.getExternalStorageDirectory().getPath() + "voice8K16bitmono.pcm";
    short sData[] = new short[BUFFER_ELEMENTS_2_REC];
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(filePath);
      while (isRecording) {
        // gets the voice output from microphone to byte format
        recorder.read(sData, 0, BUFFER_ELEMENTS_2_REC);
        Log.i(TAG, "Short writing to file" + sData.toString());
        try {
          // writes the data to file from buffer
          // stores the voice buffer
          byte bData[] = short2byte(sData);
          stream.write(bData, 0, BUFFER_ELEMENTS_2_REC * BYTES_PER_ELEMENT);
        } catch (IOException e) {
          Log.e(TAG, e.getMessage(), e);
        }
      }
    } catch (FileNotFoundException e) {
      Log.e(TAG, e.getMessage(), e);
    }
    try {
      if (stream != null) {
        stream.close();
      }
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
    }
  }

  private void stopRecording() {
    if (null != recorder) {
      isRecording = false;
      recorder.stop();
      recorder.release();
      recorder = null;
      recordingThread = null;
    }
  }

  private View.OnClickListener btnClick = new View.OnClickListener() {
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.startButton: {
          enableButtons(true);
          startRecording();
          break;
        }
        case R.id.playButton: {
          enableButtons(false);
          stopRecording();
          break;
        }
      }
    }
  };

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      finish();
    }
    return super.onKeyDown(keyCode, event);
  }
}