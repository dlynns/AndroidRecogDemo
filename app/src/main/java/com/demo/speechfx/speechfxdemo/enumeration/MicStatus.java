package com.demo.speechfx.speechfxdemo.enumeration;

import com.demo.speechfx.speechfxdemo.R;

/**
 * Created by Marlon on 1/8/2015.
 * Mic status
 */
public enum MicStatus {GRAY(R.drawable.mic_gray), GREEN(R.drawable.mic_green), RED(R.drawable.mic_red);

  int resource;

  MicStatus(int resource) {
    this.resource = resource;
  }

  public int getResource() {
    return resource;
  }
}
