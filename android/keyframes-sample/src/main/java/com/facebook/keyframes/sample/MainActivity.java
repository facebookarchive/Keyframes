package com.facebook.keyframes.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.facebook.keyframes.ReactionsFaceDrawable;
import com.facebook.keyframes.data.ReactionsFace;
import com.facebook.keyframes.deserializers.ReactionsFaceDeserializer;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ReactionsFaceDrawable mLikeImageDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView) findViewById(R.id.sample_image_view);
        mLikeImageDrawable = new ReactionsFaceDrawable(getSampleLike());
        imageView.setImageDrawable(mLikeImageDrawable);
        mLikeImageDrawable.startAnimation();
    }

    private ReactionsFace getSampleLike() {
        InputStream stream = null;
        try {
            stream = getResources().getAssets().open("sample_haha");
            ReactionsFace likeImage = ReactionsFaceDeserializer.deserialize(stream);
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
