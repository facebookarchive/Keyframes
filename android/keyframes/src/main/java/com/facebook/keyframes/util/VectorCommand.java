/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.keyframes.util;


import java.util.Locale;

import com.facebook.keyframes.KFPath;

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

  /**
   * This function converts a lower order argument array to a higher one.
   * @param fromArgs The original arguments
   * @param destArgs The array to convert fromArgs upwards into
   * @return destArgs representing the original arguments in higher order form.
   */
  public static float[] convertUp(float[] startPoint, float[] fromArgs, float[] destArgs) {
    if (fromArgs.length >= destArgs.length) {
      throw new IllegalArgumentException("convertUp should only be called to convert a lower " +
          "order argument array to a higher one.");
    }
    if (fromArgs.length == 2) {
      if (destArgs.length == 4) {
        destArgs[0] = (startPoint[0] + fromArgs[0]) / 2f;
        destArgs[1] = (startPoint[1] + fromArgs[1]) / 2f;
        destArgs[2] = fromArgs[0];
        destArgs[3] = fromArgs[1];
      } else if (destArgs.length == 6) {
        destArgs[0] = startPoint[0] + (fromArgs[0] - startPoint[0]) / 3f;
        destArgs[1] = startPoint[1] + (fromArgs[1] - startPoint[1]) / 3f;
        destArgs[2] = fromArgs[0] + (startPoint[0] - fromArgs[0]) / 3f;
        destArgs[3] = fromArgs[1] + (startPoint[1] - fromArgs[1]) / 3f;
        destArgs[4] = fromArgs[0];
        destArgs[5] = fromArgs[1];
      } else {
        throw new IllegalArgumentException(String.format(
            "Unknown conversion from %d args to %d",
            fromArgs.length,
            destArgs.length));
      }
    } else if (fromArgs.length == 4) {
      if (destArgs.length == 6) {
        destArgs[0] = startPoint[0] + 2f / 3f * (fromArgs[0] - startPoint[0]);
        destArgs[1] = startPoint[1] + 2f / 3f * (fromArgs[1] - startPoint[1]);
        destArgs[2] = fromArgs[2] + 2f / 3f * (fromArgs[0] - fromArgs[2]);
        destArgs[3] = fromArgs[3] + 2f / 3f * (fromArgs[1] - fromArgs[3]);
        destArgs[4] = fromArgs[2];
        destArgs[5] = fromArgs[3];
      } else {
        throw new IllegalArgumentException(String.format(
            "Unknown conversion from %d args to %d",
            fromArgs.length,
            destArgs.length));
      }
    } else {
      throw new IllegalArgumentException(String.format(
          "Unknown conversion from %d args to %d",
          fromArgs.length,
          destArgs.length));
    }
    return destArgs;
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
  public abstract void apply(KFPath path);

  /**
   * A protected method that essentially is a 'static' method describing how to apply this command,
   * given a set of arguments and the format of the arguments, to a path.  This allows us to have a
   * generic interpolate method for all commands.
   */
  protected abstract void applyInner(KFPath path, ArgFormat format, float[] args);

  /**
   * Returns the number of arguments for this particular vector command.
   */
  private int getArgumentCount() {
    return mArgs.length;
  }

  /**
   * Calculates the path from transitioning from current path to the passed in path given the
   * current progress, then writes the result into the destPath.  The two commands must be the same.
   */
  public void interpolate(
      VectorCommand toCommand,
      float progress,
      KFPath destPath) {
    if (mArgFormat != toCommand.mArgFormat) {
      throw new IllegalArgumentException(
          "Argument format must match between interpolated commands. RELATIVE and ABSOLUTE " +
              "coordinates should stay consistent");
    }
    float[] fromArgs;
    float[] toArgs;
    float[] destArgs;
    VectorCommand higherOrderCommand;
    if (this.getArgumentCount() > toCommand.getArgumentCount()) {
      // This command is higher order than toCommand.
      fromArgs = mArgs;
      toArgs = convertUp(destPath.getLastPoint(), toCommand.mArgs, getRecyclableArgArray());
      destArgs = getRecyclableArgArray();
      higherOrderCommand = this;
    } else if (this.getArgumentCount() < toCommand.getArgumentCount()) {
      // This command is a lower order than toCommand
      fromArgs = convertUp(destPath.getLastPoint(), mArgs, toCommand.getRecyclableArgArray());
      toArgs = toCommand.mArgs;
      destArgs = toCommand.getRecyclableArgArray();
      higherOrderCommand = toCommand;
    } else {
      fromArgs = mArgs;
      toArgs = toCommand.mArgs;
      destArgs = getRecyclableArgArray();
      higherOrderCommand = this;
    }
    for (int i = 0, len = destArgs.length; i < len; i++) {
      destArgs[i] = interpolateValue(fromArgs[i], toArgs[i], progress);
    }
    higherOrderCommand.applyInner(destPath, mArgFormat, destArgs);
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
    public void apply(KFPath path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(KFPath path, ArgFormat format, float[] args) {
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

    @Override
    public void interpolate(
        VectorCommand toCommand,
        float progress,
        KFPath destPath) {
      if (!(toCommand instanceof MoveToCommand)) {
        throw new IllegalArgumentException("MoveToCommand should only be interpolated with other " +
            "instances of MoveToCommand");
      }
      super.interpolate(toCommand, progress, destPath);
    }
  }

  public static class QuadraticToCommand extends VectorCommand {

    public QuadraticToCommand(ArgFormat argFormat, float[] args) {
      super(argFormat, args);
    }

    @Override
    public void apply(KFPath path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(KFPath path, ArgFormat format, float[] args) {
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
    public void apply(KFPath path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(KFPath path, ArgFormat format, float[] args) {
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
    public void apply(KFPath path) {
      applyInner(path, mArgFormat, mArgs);
    }

    @Override
    protected void applyInner(KFPath path, ArgFormat format, float[] args) {
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
