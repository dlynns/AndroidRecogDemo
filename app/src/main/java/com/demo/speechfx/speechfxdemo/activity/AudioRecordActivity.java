package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.demo.speechfx.speechfxdemo.R;
import com.speechfxinc.voicein.android.FnxCore;
import com.speechfxinc.voicein.android.FnxMemFileMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;


import static com.speechfxinc.voicein.android.FonixCore.create;
import static com.speechfxinc.voicein.android.FonixCore.createWordNodeEx;
import static com.speechfxinc.voicein.android.FonixCore.destroy;
import static com.speechfxinc.voicein.android.FonixCore.getFirstResult;
import static com.speechfxinc.voicein.android.FonixCore.setAsrDictionaryFile;
import static com.speechfxinc.voicein.android.FonixCore.setGeneralNNetFile;
import static com.speechfxinc.voicein.android.FonixCore.setNodeActivePercent;
import static com.speechfxinc.voicein.android.FonixCore.setNodeRawAudioInput;
import static com.speechfxinc.voicein.android.FonixCore.setRejectionStrength;
import static com.speechfxinc.voicein.android.FonixCore.setReplayInput;
import static com.speechfxinc.voicein.android.FonixCore.setSharedPath;
import static com.speechfxinc.voicein.android.FonixCoreConstants.MAX_SPEED_FRAME_SIZE_15;
import static com.speechfxinc.voicein.android.FonixCoreConstants.MAX_SPEED_PRUNE_NNET;
import static com.speechfxinc.voicein.android.FonixPublic.combineMemFileMaps;
import static com.speechfxinc.voicein.android.FonixPublic.memFileRegister;
import static com.speechfxinc.voicein.android.FonixPublic.memFileUnregister;

//    sampleRate: 16000
//    ENCODING_PCM_16BIT
//    CHANNEL_CONFIGURATION_MONO = 2;
//    CHANNEL_IN_MONO = 16;

public class AudioRecordActivity extends Activity {
  private static final String TAG = AudioRecordActivity.class.getSimpleName();
  private static final String SHARED_PATH = "";
  private static final String ASR_DICTIONARY = "Colors.pdc";  /* A pared down user dictionary that contains color words. */
  private static final String WAVE_FILE = "green8k.wav"; /* An audio file containing the word "green". */
  private static final String GENERAL_NNET = "usgp08FN3108.psi"; /* The neural net file. */
  private static final String GENERAL_NNET_DESCR = GENERAL_NNET.substring(0, GENERAL_NNET.lastIndexOf('.'));

  private static final int RECORDER_SAMPLE_RATE = 8000;
  private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
  private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private static final int BUFFER_ELEMENTS_2_REC = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
  private static final int BYTES_PER_ELEMENT = 2; // 2 bytes in 16bit format
  private MediaPlayer player = null;
  private AudioRecord recorder = null;
  private Thread recordingThread = null;
  private boolean isRecording = false;
  private ArrayList<FnxMemFileMapping> memFileMappings;
  private static final String FILE_PATH = "/sdcard/voice8K16bitmono.pcm";

  /*
   * On Android, an application doesn't generally have direct access to the filesystem.
   * So the files are read from assets into a FnxMemFileMapping[].  The FnxMemFileMapping[] is
   * a mock filesystem in memory where each element acts like a File.  If for some reason
   * you do have access to the filesystem and can read neural nets and dictionaries
   * from there, you can use File objects, and bypass making a FnxMemFileMapping[].
   */
  private ArrayList<FnxMemFileMapping> unpackAssets(AssetManager assetManager) {
    ArrayList<FnxMemFileMapping> memFileMappings = new ArrayList<FnxMemFileMapping>();
    ArrayList<Byte> byteList = new ArrayList<Byte>();
    byte buffer[] = new byte[256];
    String files[] = new String[]{ASR_DICTIONARY, WAVE_FILE, GENERAL_NNET};
      /* Read out each file in the asset and allocate a FnxMemFileMapping object.
       * This is so that data can be read out of the mock files in the filesystem.
       */
    for (String filename : files) {
      Byte[] byteArray;
      try {
        InputStream str = assetManager.open(filename);
        int bytesRead;
        while ((bytesRead = str.read(buffer)) != -1) {
          byteArray = new Byte[bytesRead];
          for (int i = 0; i < bytesRead; ++i) {
            byteArray[i] = buffer[i];
          }
          byteList.addAll(Arrays.asList(byteArray));
        }
        str.close();
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
      }
      byteArray = byteList.toArray(new Byte[byteList.size()]);
      byte[] newByteArray = new byte[byteArray.length];
      for (int i = 0; i < byteArray.length; ++i) {
        newByteArray[i] = byteArray[i];
      }
      memFileMappings.add(new FnxMemFileMapping(filename, newByteArray));
      byteList.clear();
    }
    return memFileMappings;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.some_layout);
    /* read the neural net, .pdc file, and audio */
    memFileMappings = unpackAssets(getAssets());
    setButtonHandlers();
    enableButtons(false);
//    int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
  }

  private String getMessage(short[] sData) {
    String message;
        /* N.B. You might not want to do it in exactly this fashion for a real application,
         at least with respect to how the UI gets set up. */
    FnxCore fnxCore = create();
    if (fnxCore != null) {
      String recognizedWord[] = new String[1]; // Buffer to contain recognized word
              /* You must always use FnxCombineMemFileMaps and FnxMemFileRegister
                 when working with FnxMemFileMapping[].  FnxCombineMemFileMaps makes a
                 contiguous array of structs in the JNI layer for the mock filesystem.
                 This array is the returned ByteBuffer */
      ByteBuffer buffer = combineMemFileMaps(memFileMappings);
      memFileRegister(buffer);  // Now the mock files are visible to other Fnx* function calls.

      Log.d(TAG, "setSharedPath " + setSharedPath(fnxCore, new File(SHARED_PATH)));  // Set shared path to location of neural nets, dictionaries, etc.
      Log.d(TAG, "setReplayInput " + setReplayInput(fnxCore, false)); // replay does not work yet...
      Log.d(TAG, "setRejectionStrength " + setRejectionStrength(fnxCore, 40));

      int generalNNetID = setGeneralNNetFile(fnxCore, GENERAL_NNET_DESCR, MAX_SPEED_PRUNE_NNET | MAX_SPEED_FRAME_SIZE_15);  // set the neural net
      Log.d(TAG, "setGeneralNNetFile " + generalNNetID);
      int nRet = setAsrDictionaryFile(fnxCore, new File(ASR_DICTIONARY));  // set the dictionary
      Log.d(TAG, "setAsrDictionaryFile " + nRet);
      int wordNodeID = createWordNodeEx(fnxCore, new String[]{"red", "green", "blue"}, null, false, null, generalNNetID);  // create a word node
      Log.d(TAG, "createWordNodeEx " + wordNodeID);
      Log.d(TAG, "setNodeActivePercent " + setNodeActivePercent(fnxCore, wordNodeID, 30));  //used to increase the speed of recognition

      Formatter formatter = new Formatter(new StringBuilder());

      // set the input to the list of waves
//      if ((nRet = setWaveInput(fnxCore, new File[]{new File(WAVE_FILE)})) < 0) {  // set audio input to the file
//        formatter.format("setWaveInput error %d\n", nRet);
//        message = formatter.toString();
//      }
      nRet = setNodeRawAudioInput(fnxCore, 0, sData, sData.length);
      Log.d(TAG, "setNodeRawAudioInput " + nRet);
//      if ((nRet = runNode(fnxCore, wordNodeID, null)) < 0) {  // process the audio input
//        formatter.format("runNode error %d\n", nRet);
//        message = formatter.toString();
//      }

      nRet = getFirstResult(fnxCore, recognizedWord, null, null, null, null, null); // get recognition results
      Log.d(TAG, "getFirstResult " + nRet);

      if (recognizedWord[0] != null) {
        formatter.format("You said, %s", recognizedWord[0]);
        message = formatter.toString();
      } else {
        message = "No word was recognized.";
      }

      destroy(fnxCore); // clean up the FnxCore object
      memFileUnregister(buffer); // unregister the memory buffer
      formatter.close();
    } else {
      message = "create() failed";
    }
    return message;
  }

  private void setButtonHandlers() {
    findViewById(R.id.startButton).setOnClickListener(btnClick);
    findViewById(R.id.stopButton).setOnClickListener(btnClick);
    findViewById(R.id.playButton).setOnClickListener(btnClick);
  }

  private void enableButtons(boolean isRecording) {
    enableButton(R.id.startButton, !isRecording);
    enableButton(R.id.stopButton, isRecording);
    enableButton(R.id.playButton, !isRecording);
  }

  private void enableButton(int id, boolean isEnable) {
    findViewById(id).setEnabled(isEnable);
  }

//  According to the javadocs, all devices are guaranteed to support this format (for recording):
//  44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT.
//  Change to CHANNEL_OUT_MONO for playback.

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
    List<short[]> list = new ArrayList<short[]>();
    // Write the output audio in byte
    int totalCount = 0;
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(FILE_PATH);
      while (isRecording) {
        short[] sData = new short[BUFFER_ELEMENTS_2_REC];
        // gets the voice output from microphone to byte format
        int count = recorder.read(sData, 0, BUFFER_ELEMENTS_2_REC);
        list.add(sData);
        Log.i(TAG, count + " bytes read");
        totalCount += count;
        try {
          // writes the data to file from buffer
          // stores the voice buffer
          byte bData[] = short2byte(sData);
          stream.write(bData, 0, BUFFER_ELEMENTS_2_REC * BYTES_PER_ELEMENT);
        } catch (IOException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          Log.e(TAG, e.getMessage(), e);
        }
      }
    } catch (FileNotFoundException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.e(TAG, e.getMessage(), e);
    }
    try {
      if (stream != null) {
        stream.close();
      }
    } catch (IOException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.e(TAG, e.getMessage(), e);
    }

    short[] theWholeThing = new short[list.size() * BUFFER_ELEMENTS_2_REC];
    for (int j = 0; j < list.size(); j++) {
      short[] data = list.get(j);
      for (int i = 0; i < data.length; i++) {
        theWholeThing[(j * i) + i] = data[i];
      }
    }
    Log.i(TAG, "getMessage " + getMessage(theWholeThing));
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

  private void playRecording() {
    player = new MediaPlayer();
    try {
      player.setDataSource(FILE_PATH);
      player.prepare();
      player.start();
      player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
          player.release();
          player = null;
        }
      });
    } catch (IOException e) {
      Log.e(TAG, "prepare() failed");
    }
  }

  private View.OnClickListener btnClick = new View.OnClickListener() {
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.startButton:
          enableButtons(true);
          startRecording();
          break;
        case R.id.stopButton:
          enableButtons(false);
          stopRecording();
          break;
        case R.id.playButton:
//          enableButtons(true);
//          playRecording();
          break;
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

//  @Override
//  public void onResume() {
//    isRecording = true;
//  }

//  @Override
//  public void onPause() {
//    isRecording = false;
//  }


//  private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
//  public AudioRecord findAudioRecord() {
//    for (int rate : mSampleRates) {
//      for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
//        for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
//          try {
//            Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
//                + channelConfig);
//            int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
//
//            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
//              // check if we can instantiate and have a success
//              AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
//
//              if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
//                return recorder;
//            }
//          } catch (Exception e) {
//            Log.e(C.TAG, rate + "Exception, keep trying.",e);
//          }
//        }
//      }
//    }
//    return null;
//  }
//
//  AudioRecord recorder = findAudioRecord();
//  recorder.release();


//==========================================================================================================================
//==========================================================================================================================


//import java.io.File;
//    import java.io.IOException;
//    import java.io.RandomAccessFile;
//
//    import android.media.AudioFormat;
//    import android.media.AudioRecord;
//    import android.media.MediaRecorder;
//    import android.media.MediaRecorder.AudioSource;
//    import android.util.Log;
//
//public class ExtAudioRecorder {
//  private final static int[] sampleRates = {44100, 22050, 11025, 8000};
//
//  public static ExtAudioRecorder getInstanse(Boolean recordingCompressed) {
//    ExtAudioRecorder result = null;
//
//    if (recordingCompressed) {
//      result = new ExtAudioRecorder(false,
//          AudioSource.MIC,
//          sampleRates[3],
//          AudioFormat.CHANNEL_CONFIGURATION_MONO,
//          AudioFormat.ENCODING_PCM_16BIT);
//    } else {
//      int i = 0;
//      do {
//        result = new ExtAudioRecorder(true,
//            AudioSource.MIC,
//            sampleRates[i],
//            AudioFormat.CHANNEL_CONFIGURATION_MONO,
//            AudioFormat.ENCODING_PCM_16BIT);
//
//      } while ((++i < sampleRates.length) & !(result.getState() == ExtAudioRecorder.State.INITIALIZING));
//    }
//    return result;
//  }
//
//  /**
//   * INITIALIZING : recorder is initializing;
//   * READY : recorder has been initialized, recorder not yet started
//   * RECORDING : recording
//   * ERROR : reconstruction needed
//   * STOPPED: reset needed
//   */
//  public enum State {
//    INITIALIZING, READY, RECORDING, ERROR, STOPPED
//  }
//
//  ;
//
//  public static final boolean RECORDING_UNCOMPRESSED = true;
//  public static final boolean RECORDING_COMPRESSED = false;
//
//  // The interval in which the recorded samples are output to the file
//  // Used only in uncompressed mode
//  private static final int TIMER_INTERVAL = 120;
//
//  // Toggles uncompressed recording on/off; RECORDING_UNCOMPRESSED / RECORDING_COMPRESSED
//  private boolean rUncompressed;
//
//  // Recorder used for uncompressed recording
//  private AudioRecord audioRecorder = null;
//
//  // Recorder used for compressed recording
//  private MediaRecorder mediaRecorder = null;
//
//  // Stores current amplitude (only in uncompressed mode)
//  private int cAmplitude = 0;
//
//  // Output file path
//  private String filePath = null;
//
//  // Recorder state; see State
//  private State state;
//
//  // File writer (only in uncompressed mode)
//  private RandomAccessFile randomAccessWriter;
//
//  // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
//  private short nChannels;
//  private int sRate;
//  private short bSamples;
//  private int bufferSize;
//  private int aSource;
//  private int aFormat;
//
//  // Number of frames written to file on each output(only in uncompressed mode)
//  private int framePeriod;
//
//  // Buffer for output(only in uncompressed mode)
//  private byte[] buffer;
//
//  // Number of bytes written to file after header(only in uncompressed mode)
//  // after stop() is called, this size is written to the header/data chunk in the wave file
//  private int payloadSize;
//
//  /**
//   * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
//   * Useful, as no exceptions are thrown.
//   *
//   * @return recorder state
//   */
//  public State getState() {
//    return state;
//  }
//
//  /*
//  *
//  * Method used for recording.
//  *
//  */
//  private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
//    public void onPeriodicNotification(AudioRecord recorder) {
//      audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
//      try {
//        randomAccessWriter.write(buffer); // Write buffer to file
//        payloadSize += buffer.length;
//        if (bSamples == 16) {
//          for (int i = 0; i < buffer.length / 2; i++) { // 16bit sample size
//            short curSample = getShort(buffer[i * 2], buffer[i * 2 + 1]);
//            if (curSample > cAmplitude) { // Check amplitude
//              cAmplitude = curSample;
//            }
//          }
//        } else { // 8bit sample size
//          for (int i = 0; i < buffer.length; i++) {
//            if (buffer[i] > cAmplitude) { // Check amplitude
//              cAmplitude = buffer[i];
//            }
//          }
//        }
//      } catch (IOException e) {
//        Log.e(ExtAudioRecorder.class.getName(), "Error occured in updateListener, recording is aborted");
//        //stop();
//      }
//    }
//
//    public void onMarkerReached(AudioRecord recorder) {
//      // NOT USED
//    }
//  };
//
//  /**
//   * Default constructor
//   * <p/>
//   * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
//   * In case of errors, no exception is thrown, but the state is set to ERROR
//   */
//  public ExtAudioRecorder(boolean uncompressed, int audioSource, int sampleRate, int channelConfig, int audioFormat) {
//    try {
//      rUncompressed = uncompressed;
//      if (rUncompressed) { // RECORDING_UNCOMPRESSED
//        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
//          bSamples = 16;
//        } else {
//          bSamples = 8;
//        }
//
//        if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
//          nChannels = 1;
//        } else {
//          nChannels = 2;
//        }
//
//        aSource = audioSource;
//        sRate = sampleRate;
//        aFormat = audioFormat;
//
//        framePeriod = sampleRate * TIMER_INTERVAL / 1000;
//        bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
//        if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) { // Check to make sure buffer size is not smaller than the smallest allowed one
//          bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
//          // Set frame period and timer interval accordingly
//          framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
//          Log.w(ExtAudioRecorder.class.getName(), "Increasing buffer size to " + Integer.toString(bufferSize));
//        }
//
//        audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
//
//        if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
//          throw new Exception("AudioRecord initialization failed");
//        audioRecorder.setRecordPositionUpdateListener(updateListener);
//        audioRecorder.setPositionNotificationPeriod(framePeriod);
//      } else { // RECORDING_COMPRESSED
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//      }
//      cAmplitude = 0;
//      filePath = null;
//      state = State.INITIALIZING;
//    } catch (Exception e) {
//      if (e.getMessage() != null) {
//        Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
//      } else {
//        Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured while initializing recording");
//      }
//      state = State.ERROR;
//    }
//  }
//
//  /**
//   * Sets output file path, call directly after construction/reset.
//   *
//   * @param output file path
//   */
//  public void setOutputFile(String argPath) {
//    try {
//      if (state == State.INITIALIZING) {
//        filePath = argPath;
//        if (!rUncompressed) {
//          mediaRecorder.setOutputFile(filePath);
//        }
//      }
//    } catch (Exception e) {
//      if (e.getMessage() != null) {
//        Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
//      } else {
//        Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured while setting output path");
//      }
//      state = State.ERROR;
//    }
//  }
//
//  /**
//   * Returns the largest amplitude sampled since the last call to this method.
//   *
//   * @return returns the largest amplitude since the last call, or 0 when not in recording state.
//   */
//  public int getMaxAmplitude() {
//    if (state == State.RECORDING) {
//      if (rUncompressed) {
//        int result = cAmplitude;
//        cAmplitude = 0;
//        return result;
//      } else {
//        try {
//          return mediaRecorder.getMaxAmplitude();
//        } catch (IllegalStateException e) {
//          return 0;
//        }
//      }
//    } else {
//      return 0;
//    }
//  }
//
//
//  /**
//   * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
//   * the recorder is set to the ERROR state, which makes a reconstruction necessary.
//   * In case uncompressed recording is toggled, the header of the wave file is written.
//   * In case of an exception, the state is changed to ERROR
//   */
//  public void prepare() {
//    try {
//      if (state == State.INITIALIZING) {
//        if (rUncompressed) {
//          if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
//            // write file header
//
//            randomAccessWriter = new RandomAccessFile(filePath, "rw");
//
//            randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
//            randomAccessWriter.writeBytes("RIFF");
//            randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
//            randomAccessWriter.writeBytes("WAVE");
//            randomAccessWriter.writeBytes("fmt ");
//            randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
//            randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
//            randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
//            randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
//            randomAccessWriter.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
//            randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8))); // Block align, NumberOfChannels*BitsPerSample/8
//            randomAccessWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
//            randomAccessWriter.writeBytes("data");
//            randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0
//
//            buffer = new byte[framePeriod * bSamples / 8 * nChannels];
//            state = State.READY;
//          } else {
//            Log.e(ExtAudioRecorder.class.getName(), "prepare() method called on uninitialized recorder");
//            state = State.ERROR;
//          }
//        } else {
//          mediaRecorder.prepare();
//          state = State.READY;
//        }
//      } else {
//        Log.e(ExtAudioRecorder.class.getName(), "prepare() method called on illegal state");
//        release();
//        state = State.ERROR;
//      }
//    } catch (Exception e) {
//      if (e.getMessage() != null) {
//        Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
//      } else {
//        Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured in prepare()");
//      }
//      state = State.ERROR;
//    }
//  }
//
//  /**
//   * Releases the resources associated with this class, and removes the unnecessary files, when necessary
//   */
//  public void release() {
//    if (state == State.RECORDING) {
//      stop();
//    } else {
//      if ((state == State.READY) & (rUncompressed)) {
//        try {
//          randomAccessWriter.close(); // Remove prepared file
//        } catch (IOException e) {
//          Log.e(ExtAudioRecorder.class.getName(), "I/O exception occured while closing output file");
//        }
//        (new File(filePath)).delete();
//      }
//    }
//
//    if (rUncompressed) {
//      if (audioRecorder != null) {
//        audioRecorder.release();
//      }
//    } else {
//      if (mediaRecorder != null) {
//        mediaRecorder.release();
//      }
//    }
//  }
//
//  /**
//   * Resets the recorder to the INITIALIZING state, as if it was just created.
//   * In case the class was in RECORDING state, the recording is stopped.
//   * In case of exceptions the class is set to the ERROR state.
//   */
//  public void reset() {
//    try {
//      if (state != State.ERROR) {
//        release();
//        filePath = null; // Reset file path
//        cAmplitude = 0; // Reset amplitude
//        if (rUncompressed) {
//          audioRecorder = new AudioRecord(aSource, sRate, nChannels + 1, aFormat, bufferSize);
//        } else {
//          mediaRecorder = new MediaRecorder();
//          mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//          mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//          mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        }
//        state = State.INITIALIZING;
//      }
//    } catch (Exception e) {
//      Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
//      state = State.ERROR;
//    }
//  }
//
//  /**
//   * Starts the recording, and sets the state to RECORDING.
//   * Call after prepare().
//   */
//  public void start() {
//    if (state == State.READY) {
//      if (rUncompressed) {
//        payloadSize = 0;
//        audioRecorder.startRecording();
//        audioRecorder.read(buffer, 0, buffer.length);
//      } else {
//        mediaRecorder.start();
//      }
//      state = State.RECORDING;
//    } else {
//      Log.e(ExtAudioRecorder.class.getName(), "start() called on illegal state");
//      state = State.ERROR;
//    }
//  }
//
//  /**
//   * Stops the recording, and sets the state to STOPPED.
//   * In case of further usage, a reset is needed.
//   * Also finalizes the wave file in case of uncompressed recording.
//   */
//  public void stop() {
//    if (state == State.RECORDING) {
//      if (rUncompressed) {
//        audioRecorder.stop();
//
//        try {
//          randomAccessWriter.seek(4); // Write size to RIFF header
//          randomAccessWriter.writeInt(Integer.reverseBytes(36 + payloadSize));
//
//          randomAccessWriter.seek(40); // Write size to Subchunk2Size field
//          randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));
//
//          randomAccessWriter.close();
//        } catch (IOException e) {
//          Log.e(ExtAudioRecorder.class.getName(), "I/O exception occured while closing output file");
//          state = State.ERROR;
//        }
//      } else {
//        mediaRecorder.stop();
//      }
//      state = State.STOPPED;
//    } else {
//      Log.e(ExtAudioRecorder.class.getName(), "stop() called on illegal state");
//      state = State.ERROR;
//    }
//  }
//
//  /* Converts a byte[2] to a short, in LITTLE_ENDIAN format */
//  private short getShort(byte argB1, byte argB2) {
//    return (short) (argB1 | (argB2 << 8));
//  }
//}