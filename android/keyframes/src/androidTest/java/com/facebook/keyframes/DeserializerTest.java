package com.facebook.keyframes;

import android.test.AndroidTestCase;

import com.facebook.keyframes.data.ReactionsFace;
import com.facebook.keyframes.deserializers.ReactionsFaceDeserializer;

import junit.framework.Assert;

import java.io.InputStream;

public class DeserializerTest extends AndroidTestCase {

    public void testDeserializeValidFile() throws Exception {
        InputStream stream = getContext().getResources().getAssets().open("sample_like");
        ReactionsFace deserializedModel = ReactionsFaceDeserializer.deserialize(stream);
        Assert.assertNotNull(deserializedModel);
    }

    // TODO: Test and catch various arg errors
}
