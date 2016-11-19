/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Region;

import com.facebook.keyframes.util.MatrixUtils;

import java.util.List;

/**
 * A utility class for drawing a list of {@link KeyframesDrawable.FeatureState}s onto a canvas.
 */
public class KFFeatureDrawer {

  private final Paint mDrawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Matrix mRecyclableInverseMatrix = new Matrix();

  public KFFeatureDrawer() {
    mDrawingPaint.setStrokeCap(Paint.Cap.ROUND);
  }

  /**
   * Draw the features onto the provided canvas, at the scale provided by the scaleMatrix param.
   */
  public void drawFeaturesToCanvas(
      Canvas canvas,
      List<KeyframesDrawable.FeatureState> featureStateList,
      Matrix scaleMatrix) {
    scaleMatrix.invert(mRecyclableInverseMatrix);

    KFPath pathToDraw;
    KeyframesDrawable.FeatureState featureState;
    for (int i = 0, len = featureStateList.size(); i < len; i++) {
      featureState = featureStateList.get(i);
      if (!featureState.isVisible()) {
        continue;
      }

      final KeyframesDrawable.FeatureConfig config = featureState.getConfig();
      final Matrix uniqueFeatureMatrix = featureState.getUniqueFeatureMatrix();
      if (config != null && config.drawable != null && uniqueFeatureMatrix != null) {
        // This block is for the experimental particle effects.
        canvas.save();
        canvas.concat(scaleMatrix);
        canvas.concat(uniqueFeatureMatrix);

        final boolean shouldApplyMatrix = config.matrix != null && !config.matrix.isIdentity();
        if (shouldApplyMatrix) {
          canvas.save();
          canvas.concat(config.matrix);
        }
        config.drawable.draw(canvas);
        if (shouldApplyMatrix) {
          canvas.restore();
        }

        canvas.restore();
        continue;
      }

      pathToDraw = featureState.getCurrentPathForDrawing();
      if (pathToDraw == null || pathToDraw.isEmpty()) {
        continue;
      }
      if (featureState.getCurrentMaskPath() != null) {
        canvas.save();
        applyScaleAndClipCanvas(
            canvas,
            scaleMatrix,
            mRecyclableInverseMatrix,
            featureState.getCurrentMaskPath(),
            Region.Op.INTERSECT);
      }
      mDrawingPaint.setShader(null);
      mDrawingPaint.setStrokeCap(featureState.getStrokeLineCap());
      if (featureState.getFillColor() != Color.TRANSPARENT) {
        mDrawingPaint.setStyle(Paint.Style.FILL);
        if (featureState.getCurrentShader() == null) {
          mDrawingPaint.setColor(featureState.getFillColor());
          mDrawingPaint.setAlpha(featureState.getAlpha());
          applyScaleAndDrawPath(
              canvas,
              scaleMatrix,
              mRecyclableInverseMatrix,
              pathToDraw,
              mDrawingPaint);
        } else {
          mDrawingPaint.setShader(featureState.getCurrentShader());
          applyScaleToCanvasAndDrawPath(
              canvas,
              scaleMatrix,
              mRecyclableInverseMatrix,
              pathToDraw,
              mDrawingPaint);
        }
      }
      if (featureState.getStrokeColor() != Color.TRANSPARENT && featureState.getStrokeWidth() > 0) {
        mDrawingPaint.setColor(featureState.getStrokeColor());
        mDrawingPaint.setAlpha(featureState.getAlpha());
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setStrokeWidth(
            featureState.getStrokeWidth() * MatrixUtils.extractScaleFromMatrix(scaleMatrix));
        applyScaleAndDrawPath(
            canvas,
            scaleMatrix,
            mRecyclableInverseMatrix,
            pathToDraw,
            mDrawingPaint);
      }
      if (featureState.getCurrentMaskPath() != null) {
        canvas.restore();
      }
    }
  }


  private static void applyScaleAndClipCanvas(
      Canvas canvas,
      Matrix scaleMatrix,
      Matrix inverseScaleMatrix,
      KFPath path,
      Region.Op op) {
    path.transform(scaleMatrix);
    canvas.clipPath(path.getPath(), op);
    path.transform(inverseScaleMatrix);
  }

  private static void applyScaleAndDrawPath(
      Canvas canvas,
      Matrix scaleMatrix,
      Matrix inverseScaleMatrix,
      KFPath path,
      Paint paint) {
    path.transform(scaleMatrix);
    canvas.drawPath(path.getPath(), paint);
    path.transform(inverseScaleMatrix);
  }

  /**
   * Note: This method is only necessary because of cached gradient shaders with a fixed size.  We
   * need to scale the canvas in this case rather than scaling the path.
   */
  private static void applyScaleToCanvasAndDrawPath(
      Canvas canvas,
      Matrix scaleMatrix,
      Matrix inverseScaleMatrix,
      KFPath path,
      Paint paint) {
    canvas.concat(scaleMatrix);
    canvas.drawPath(path.getPath(), paint);
    canvas.concat(inverseScaleMatrix);
  }
}
