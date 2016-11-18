/* Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
 */

package com.facebook.keyframes.sample;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.facebook.keyframes.KeyframesDrawable;
import com.facebook.keyframes.KeyframesDrawableBuilder;
import com.facebook.keyframes.deserializers.KFImageDeserializer;
import com.facebook.keyframes.model.KFImage;

public class MainActivity extends Activity {

  private static final String TAG = "KeyframesSample";

  private static final int TEST_CANVAS_SIZE_PX = 500;

  private KeyframesDrawable mKeyFramesDrawable;

  private final IntentFilter mPreviewKeyframesAnimation = new IntentFilter("PreviewKeyframesAnimation");

  private BroadcastReceiver mPreviewRenderReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "received intent");

      String descriptorPath = intent.getStringExtra("descriptorPath");
      if (descriptorPath == null) {
        Log.e(TAG, "intent missing 'descriptorPath'");
        return;
      }

      requestPermission();
      InputStream descriptorJSON;
      try {
        descriptorJSON = new FileInputStream(descriptorPath);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return;
      }

      KFImage kfImage;
      try {
        kfImage = KFImageDeserializer.deserialize(descriptorJSON);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      setKFImage(kfImage);
    }

  };

  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static final String[] PERMISSIONS_STORAGE = {
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private KFImage mKfImage;

  private void requestPermission() {
    // Check if we have write permission
    int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(
              this,
              PERMISSIONS_STORAGE,
              REQUEST_EXTERNAL_STORAGE
      );
    }
  }

  private String dexOutputDir;
  private File dir;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setKFImage(getSampleImage());

    registerReceiver(mPreviewRenderReceiver, mPreviewKeyframesAnimation);
  }

  public void resetImage(View view) {
    setKFImage(mKfImage);
  }

  private void clearImage() {
    if (mKeyFramesDrawable == null) {
      return;
    }
    mKeyFramesDrawable.stopAnimation();
    mKeyFramesDrawable = null;
  }


  private void setKFImage(KFImage kfImage) {
    clearImage();
    mKfImage = kfImage;

    final Drawable logoDrawable = getResources().getDrawable(R.drawable.keyframes_launcher);
    if (logoDrawable != null) {
      logoDrawable.setBounds(0, 0, 80, 80);
      mKeyFramesDrawable = new KeyframesDrawableBuilder()
          .withImage(mKfImage)
          .withMaxFrameRate(60)
          .withExperimentalFeatures()
          .withParticleFeatureConfigs(
              Pair.create("keyframes", Pair.create(logoDrawable, new Matrix())))
          .build();
    }
    mKeyFramesDrawable.startAnimation();

    final ImageView imageView = (ImageView) findViewById(R.id.sample_image_view);
    imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    imageView.setImageDrawable(mKeyFramesDrawable);
  }

  private KFImage getSampleImage() {
    InputStream stream = null;
    try {
      stream = getResources().getAssets().open("sample_file");
      KFImage sampleImage = KFImageDeserializer.deserialize(stream);
      return sampleImage;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ignored) {
        }
      }
    }
    return null;
  }

  @Override
  public void onPause() {
    if (mKeyFramesDrawable != null) {
      mKeyFramesDrawable.stopAnimation();
    }
    unregisterReceiver(mPreviewRenderReceiver);
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    registerReceiver(mPreviewRenderReceiver, mPreviewKeyframesAnimation);
    if (mKeyFramesDrawable != null) {
      mKeyFramesDrawable.startAnimation();
    }
  }
}
