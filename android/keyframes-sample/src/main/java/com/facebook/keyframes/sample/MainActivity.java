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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.facebook.keyframes.KeyframesDrawable;
import com.facebook.keyframes.model.KFImage;
import com.facebook.keyframes.deserializers.KFImageDeserializer;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

  private KeyframesDrawable mLikeImageDrawable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ImageView imageView = (ImageView) findViewById(R.id.sample_image_view);
    mLikeImageDrawable = new KeyframesDrawable(getSampleLike());
    imageView.setImageDrawable(mLikeImageDrawable);
    mLikeImageDrawable.startAnimation();
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
}
