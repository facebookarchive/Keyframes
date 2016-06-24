/* This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.facebook.keyframes.sample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.keyframes.KeyframesDrawable;
import com.facebook.keyframes.model.KFImage;
import com.facebook.keyframes.deserializers.KFImageDeserializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

  private static final int TEST_CANVAS_SIZE_PX = 500;

  private KeyframesDrawable mLikeImageDrawable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ImageView imageView = (ImageView) findViewById(R.id.sample_image_view);
    mLikeImageDrawable = new KeyframesDrawable(getSampleLike());
    imageView.setImageDrawable(mLikeImageDrawable);
    mLikeImageDrawable.startAnimation();

    View generateStillsButton = findViewById(R.id.dev_generate_stills_button);
    generateStillsButton.setVisibility(View.GONE);
  }

  private KFImage getSampleLike() {
    InputStream stream = null;
    try {
      stream = getResources().getAssets().open("sample_anger_temp");
      KFImage likeImage = KFImageDeserializer.deserialize(stream);
      return likeImage;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
        }
      }
    }
    return null;
  }

  @Override
  public void onPause() {
    mLikeImageDrawable.stopAnimationAtLoopEnd();
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    mLikeImageDrawable.startAnimation();
  }

  /**
   * TODO TEMPORARY HACK! JUST GRABBING SNAPSHOTS!
   */
  public void generateNewTestStills(View view) {
    try {
      String storageDirectory =
          Environment.getExternalStorageDirectory().getAbsolutePath() + "/Keyframes";
      File storageDirFile = new File(storageDirectory);
      if (!storageDirFile.exists()) {
        storageDirFile.mkdir();
      }

      KFImage image = getSampleLike();
      int frameCount = image.getFrameCount();
      KeyframesDrawable drawable = new KeyframesDrawable(image);
      Bitmap bitmap =
          Bitmap.createBitmap(TEST_CANVAS_SIZE_PX, TEST_CANVAS_SIZE_PX, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);

      drawable.setBounds(0, 0, TEST_CANVAS_SIZE_PX, TEST_CANVAS_SIZE_PX);

      float step = .1f;
      for (float progress = 0; progress <= 1; progress += step) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawable.setFrameProgress(frameCount * progress);
        drawable.draw(canvas);

        File outputFile = new File(storageDirFile, "test_" + (int) (progress / step) + ".png");
        OutputStream outputStream = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
        Log.v("Keyframes Dev", "Test static image generated at: " + outputFile.getAbsolutePath());
      }
    } catch (Exception e) {

    }
  }
}
