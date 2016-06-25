/* Copyright (c) 2016, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the LICENSE file in
 * the root directory of this source tree.
 */

package com.facebook.keyframes.util;


import java.util.Locale;

import android.graphics.Path;

public abstract class VectorCommand {

  private enum SVGCommand {
    m(ArgFormat.RELATIVE, 2),
    M(ArgFormat.ABSOLUTE, 2),
    q(ArgFormat.RELATIVE, 4),
    Q(ArgFormat.ABSOLUTE, 4),
    c(ArgFormat.RELATIVE, 6),
    C(ArgFormat.ABSOLUTE, 6),
    l(ArgFormat.RELATIVE, 2),
    L(ArgFormat.ABSOLUTE, 2);

    public final ArgFormat argFormat;
    public final int argCount;

    SVGCommand(ArgFormat argFormat, int argCount) {
      this.argFormat = argFormat;
      this.argCount = argCount;
    }
  }

  enum ArgFormat {
    RELATIVE,
    ABSOLUTE
  }

  private static final String SVG_ARG_DELIMITER = ",";

  public static VectorCommand createVectorCommand(String svgCommandString) {
    SVGCommand cmd = SVGCommand.valueOf(svgCommandString.substring(0, 1));
    String[] argsAsString = svgCommandString.substring(1).split(SVG_ARG_DELIMITER);
    float[] args = new float[argsAsString.length];
    int i = 0;
    for (String arg : argsAsString) {
      args[i++] = Float.parseFloat(arg);
    }
    switch (cmd) {
      case m:
      case M: {
        if (checkArguments(cmd, args)) {
          return new MoveToCommand(cmd.argFormat, args);
        } else {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "VectorCommand MoveTo requires two arguments, but got %s",
              args.toString()));
        }
      }
      case q:
      case Q: {
        if (checkArguments(cmd, args)) {
          return new QuadraticToCommand(cmd.argFormat, args);
        } else {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "VectorCommand QuadraticTo requires four arguments, but got %s",
              args.toString()));
        }
      }
      case c:
      case C: {
        if (checkArguments(cmd, args)) {
          return new CubicToCommand(cmd.argFormat, args);
        } else {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "VectorCommand CubicTo requires six arguments, but got %s",
              args.toString()));
        }
      }
      case l:
      case L: {
        if (checkArguments(cmd, args)) {
          return new LineToCommand(cmd.argFormat, args);
        } else {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "VectorCommand LineTo requires two arguments, but got %s",
              args.toString()));
        }
      }
      default: {
        throw new IllegalArgumentException(String.format(
            Locale.US,
            "Unhandled vector command: %s",
            svgCommandString));
      }
    }
  }

  public static boolean checkArguments(SVGCommand command, float[] args) {
    return command.argCount == args.length;
  }

  final ArgFormat mArgFormat;
  final float[] mArgs;

  private float[] mRecyclableArgArray;

  public VectorCommand(ArgFormat argFormat, float[] args) {
    mArgFormat = argFormat;
    mArgs = args;
  }

  /**
   * Applies this command to the path.
   */
  public abstract void apply(Path path);

  /**
   * A protected method that essentially is a 'static' method describing how to apply this command,
   * given a set of arguments and the format of the arguments, to a path.  This allows us to have a
   * generic interpolate method for all commands.
   */
  protected abstract void applyInner(Path path, ArgFormat format, float[] args);

  /**
   * Calculates the path from transitioning from current path to the passed in path given the
   * current progress, then writes the result into the destPath.  The two commands must be the same.
   */
  public void interpolate(
      VectorCommand command,
      float progress,
      Path destPath) {
    float[] interpolatedArgs = getRecyclableArgArray();
    for (int i = 0, len = mArgs.length; i < len; i++) {
      interpolatedArgs[i] = interpolateValue(mArgs[i], command.mArgs[i], progress);
    }
    applyInner(destPath, mArgFormat, interpolatedArgs);
  }

  private float[] getRecyclableArgArray() {
    if (mRecyclableArgArray == null) {
      mRecyclableArgArray = new float[mArgs.length];
    }
    return mRecyclableArgArray;
  }

  public static class MoveToCommand extends VectorCommand {

    public MoveToCommand(ArgFormat argFormat, float[] args) {
      super(argFormat, args);
    }

    @Override
    public void apply(Path path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(Path path, ArgFormat format, float[] args) {
      switch (format) {
        case RELATIVE: {
          path.rMoveTo(args[0], args[1]);
          break;
        }
        case ABSOLUTE: {
          path.moveTo(args[0], args[1]);
          break;
        }
        default: {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "No such argument format %s",
              format));
        }
      }
    }
  }

  public static class QuadraticToCommand extends VectorCommand {

    public QuadraticToCommand(ArgFormat argFormat, float[] args) {
      super(argFormat, args);
    }

    @Override
    public void apply(Path path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(Path path, ArgFormat format, float[] args) {
      switch (format) {
        case RELATIVE: {
          path.rQuadTo(
              args[0],
              args[1],
              args[2],
              args[3]);
          break;
        }
        case ABSOLUTE: {
          path.quadTo(
              args[0],
              args[1],
              args[2],
              args[3]);
          break;
        }
        default: {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "No such argument format %s",
              format));
        }
      }
    }
  }

  public static class CubicToCommand extends VectorCommand {

    public CubicToCommand(ArgFormat argFormat, float[] args) {
      super(argFormat, args);
    }

    @Override
    public void apply(Path path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(Path path, ArgFormat format, float[] args) {
      switch (format) {
        case RELATIVE: {
          path.rCubicTo(
              args[0],
              args[1],
              args[2],
              args[3],
              args[4],
              args[5]);
          break;
        }
        case ABSOLUTE: {
          path.cubicTo(
              args[0],
              args[1],
              args[2],
              args[3],
              args[4],
              args[5]);
          break;
        }
        default: {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "No such argument format %s",
              format));
        }
      }
    }
  }

  public static class LineToCommand extends VectorCommand {

    public LineToCommand(ArgFormat argFormat, float[] args) {
      super(argFormat, args);
    }

    @Override
    public void apply(Path path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(Path path, ArgFormat format, float[] args) {
      switch (format) {
        case RELATIVE: {
          path.rLineTo(args[0], args[1]);
          break;
        }
        case ABSOLUTE: {
          path.lineTo(args[0], args[1]);
          break;
        }
        default: {
          throw new IllegalArgumentException(String.format(
              Locale.US,
              "No such argument format %s",
              format));
        }
      }
    }
  }

  private static float interpolateValue(float valueA, float valueB, float progress) {
    return valueA + (valueB - valueA) * progress;
  }
}
