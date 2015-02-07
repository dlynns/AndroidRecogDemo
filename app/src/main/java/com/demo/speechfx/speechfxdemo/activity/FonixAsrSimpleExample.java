/**
 * @file FonixAsrSimpleExample.java
 *
 * @date 2014-01-31
 *
 * @brief Simple ASR demo describing the layout of an Android project.
 *
 * This application basically demonstrates the layout for an Android project.
 * There are two sets of files that must be added to this project: <code>voicein.jar</code>
 * and the shared libraries that <code>voicein.jar</code> depends on.
 *
 * <code>voicein.jar</code> goes in <code>libs</code>.
 * The shared libs (<code>.so</code> files) go in <code>libs/armeabi-v7a</code> (for ARM libs).
 */
package com.demo.speechfx.speechfxdemo.activity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.demo.speechfx.speechfxdemo.R;
import com.speechfxinc.voicein.android.FnxCore;
import com.speechfxinc.voicein.android.FnxMemFileMapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;


import static com.speechfxinc.voicein.android.FonixCore.MAX_SPEED_FRAME_SIZE_15;
import static com.speechfxinc.voicein.android.FonixCore.MAX_SPEED_PRUNE_NNET;
import static com.speechfxinc.voicein.android.FonixCore.create;
import static com.speechfxinc.voicein.android.FonixCore.createWordNodeEx;
import static com.speechfxinc.voicein.android.FonixCore.destroy;
import static com.speechfxinc.voicein.android.FonixCore.getFirstResult;
import static com.speechfxinc.voicein.android.FonixCore.runNode;
import static com.speechfxinc.voicein.android.FonixCore.setAsrDictionaryFile;
import static com.speechfxinc.voicein.android.FonixCore.setGeneralNNetFile;
import static com.speechfxinc.voicein.android.FonixCore.setNodeActivePercent;
import static com.speechfxinc.voicein.android.FonixCore.setRejectionStrength;
import static com.speechfxinc.voicein.android.FonixCore.setReplayInput;
import static com.speechfxinc.voicein.android.FonixCore.setSharedPath;
import static com.speechfxinc.voicein.android.FonixCore.setWaveInput;
import static com.speechfxinc.voicein.android.FonixPublic.combineMemFileMaps;
import static com.speechfxinc.voicein.android.FonixPublic.memFileRegister;
import static com.speechfxinc.voicein.android.FonixPublic.memFileUnregister;

public class FonixAsrSimpleExample extends Activity {
  private static final String TAG = FonixAsrSimpleExample.class.getSimpleName();
  private static final String SHARED_PATH = "";
  private static final String ASR_DICTIONARY = "Colors.pdc";  /* A pared down user dictionary that contains color words. */
  private static final String WAVE_FILE = "green8k.wav"; /* An audio file containing the word "green". */
  private static final String GENERAL_NNET = "usgp08FN3108.psi"; /* The neural net file. */
  private static final String GENERAL_NNET_DESCR = GENERAL_NNET.substring(0, GENERAL_NNET.lastIndexOf('.'));
  private TextView textView;
  private Button greenButton;
  ArrayList<FnxMemFileMapping> memFileMappings;


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
    setContentView(R.layout.legacy);
    /* read the neural net, .pdc file, and audio */
    memFileMappings = unpackAssets(getAssets());
    textView = (TextView) findViewById(R.id.result);
    greenButton = (Button) findViewById(R.id.button);

  }

  private String getMessage() {
    String message;
        /* N.B. You might not want to do it in exactly this fashion for a real application,
         at least with respect to how the UI gets set up. */
    FnxCore fnxCore = create();
    if (fnxCore != null) {
      String recognizedWord[] = new String[1];                            // Buffer to contain recognized word
              /* You must always use FnxCombineMemFileMaps and FnxMemFileRegister
                 when working with FnxMemFileMapping[].  FnxCombineMemFileMaps makes a
                 contiguous array of structs in the JNI layer for the mock filesystem.
                 This array is the returned ByteBuffer */
      ByteBuffer buffer = combineMemFileMaps(memFileMappings);
      memFileRegister(buffer);  // Now the mock files are visible to other Fnx* function calls.

      setSharedPath(fnxCore, new File(SHARED_PATH));  // Set shared path to location of neural nets, dictionaries, etc.
      setReplayInput(fnxCore, false); // replay does not work yet...
      setRejectionStrength(fnxCore, 40);

      int generalNNetID = setGeneralNNetFile(fnxCore, GENERAL_NNET_DESCR, MAX_SPEED_PRUNE_NNET | MAX_SPEED_FRAME_SIZE_15);  // set the neural net
      int nRet = setAsrDictionaryFile(fnxCore, new File(ASR_DICTIONARY));  // set the dictionary
      int wordNodeID = createWordNodeEx(fnxCore, new String[]{"red", "green", "blue"}, null, false, null, generalNNetID);  // create a word node
      setNodeActivePercent(fnxCore, wordNodeID, 30);  //used to increase the speed of recognition

      Formatter formatter = new Formatter(new StringBuilder());

      // set the input to the list of waves
      if ((nRet = setWaveInput(fnxCore, new File[]{new File(WAVE_FILE)})) < 0) {  // set audio input to the file
        formatter.format("setWaveInput error %d\n", nRet);
        message = formatter.toString();
      }
//        nRet = setNodeRawAudioInput(fnxCore, 0, )
      if ((nRet = runNode(fnxCore, wordNodeID, null)) < 0) {  // process the audio input
        formatter.format("runNode error %d\n", nRet);
        message = formatter.toString();
      }

      nRet = getFirstResult(fnxCore, recognizedWord, null, null, null, null, null); // get recognition results

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

  public void onClickGreen(View view) {
    String message = getMessage();
    textView.setText(message);
  }
}
