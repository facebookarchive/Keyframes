package com.facebook.keyframes.util;

import android.graphics.Path;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class VectorCommandTest {

  @Test
  public void testUnrecognizedCommand() {
    try {
      VectorCommand.createVectorCommand("A1,2");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("No enum constant"));
    }
  }

  @Test
  public void testValidParsingMove() {
    // Absolute coordinates
    VectorCommand moveToCommand = VectorCommand.createVectorCommand("M1,2");
    Assert.assertTrue(moveToCommand instanceof VectorCommand.MoveToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.ABSOLUTE, moveToCommand.mArgFormat);
    Assert.assertEquals(2, moveToCommand.mArgs.length);
    Assert.assertEquals(1, moveToCommand.mArgs[0], 0);
    Assert.assertEquals(2, moveToCommand.mArgs[1], 0);

    // Relative coordinates
    moveToCommand = VectorCommand.createVectorCommand("m3,4");
    Assert.assertTrue(moveToCommand instanceof VectorCommand.MoveToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.RELATIVE, moveToCommand.mArgFormat);
    Assert.assertEquals(2, moveToCommand.mArgs.length);
    Assert.assertEquals(3, moveToCommand.mArgs[0], 0);
    Assert.assertEquals(4, moveToCommand.mArgs[1], 0);
  }

  @Test
  public void testInvalidParsingMove() {
    // Too few arguments
    try {
      VectorCommand.createVectorCommand("M1");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires two arguments"));
    }

    try {
      VectorCommand.createVectorCommand("M1,2,3");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires two arguments"));
    }
  }

  @Test
  public void testValidParsingQuadratic() {
    // Absolute coordinates
    VectorCommand quadraticToCommand = VectorCommand.createVectorCommand("Q1,2,3,4");
    Assert.assertTrue(quadraticToCommand instanceof VectorCommand.QuadraticToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.ABSOLUTE, quadraticToCommand.mArgFormat);
    Assert.assertEquals(4, quadraticToCommand.mArgs.length);
    Assert.assertEquals(1, quadraticToCommand.mArgs[0], 0);
    Assert.assertEquals(2, quadraticToCommand.mArgs[1], 0);
    Assert.assertEquals(3, quadraticToCommand.mArgs[2], 0);
    Assert.assertEquals(4, quadraticToCommand.mArgs[3], 0);

    // Relative coordinates
    quadraticToCommand = VectorCommand.createVectorCommand("q5,6,7,8");
    Assert.assertTrue(quadraticToCommand instanceof VectorCommand.QuadraticToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.RELATIVE, quadraticToCommand.mArgFormat);
    Assert.assertEquals(4, quadraticToCommand.mArgs.length);
    Assert.assertEquals(5, quadraticToCommand.mArgs[0], 0);
    Assert.assertEquals(6, quadraticToCommand.mArgs[1], 0);
    Assert.assertEquals(7, quadraticToCommand.mArgs[2], 0);
    Assert.assertEquals(8, quadraticToCommand.mArgs[3], 0);
  }

  @Test
  public void testInvalidParsingQuadratic() {
    // Too few arguments
    try {
      VectorCommand.createVectorCommand("Q1,2");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires four arguments"));
    }

    // Too many arguments
    try {
      VectorCommand.createVectorCommand("Q1,2,3,4,5");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires four arguments"));
    }
  }

  @Test
  public void testValidParsingCubic() {
    // Absolute coordinates
    VectorCommand cubicToCommand = VectorCommand.createVectorCommand("C1,2,3,4,5,6");
    Assert.assertTrue(cubicToCommand instanceof VectorCommand.CubicToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.ABSOLUTE, cubicToCommand.mArgFormat);
    Assert.assertEquals(6, cubicToCommand.mArgs.length);
    Assert.assertEquals(1, cubicToCommand.mArgs[0], 0);
    Assert.assertEquals(2, cubicToCommand.mArgs[1], 0);
    Assert.assertEquals(3, cubicToCommand.mArgs[2], 0);
    Assert.assertEquals(4, cubicToCommand.mArgs[3], 0);
    Assert.assertEquals(5, cubicToCommand.mArgs[4], 0);
    Assert.assertEquals(6, cubicToCommand.mArgs[5], 0);

    // Relative coordinates
    cubicToCommand = VectorCommand.createVectorCommand("c7,8,9,10,11,12");
    Assert.assertTrue(cubicToCommand instanceof VectorCommand.CubicToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.RELATIVE, cubicToCommand.mArgFormat);
    Assert.assertEquals(6, cubicToCommand.mArgs.length);
    Assert.assertEquals(7, cubicToCommand.mArgs[0], 0);
    Assert.assertEquals(8, cubicToCommand.mArgs[1], 0);
    Assert.assertEquals(9, cubicToCommand.mArgs[2], 0);
    Assert.assertEquals(10, cubicToCommand.mArgs[3], 0);
    Assert.assertEquals(11, cubicToCommand.mArgs[4], 0);
    Assert.assertEquals(12, cubicToCommand.mArgs[5], 0);
  }

  @Test
  public void testInvalidParsingCubic() {
    // Too few arguments
    try {
      VectorCommand.createVectorCommand("C1,2,3,4");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires six arguments"));
    }

    // Too many arguments
    try {
      VectorCommand.createVectorCommand("C1,2,3,4,5,6,7");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires six arguments"));
    }
  }

  @Test
  public void testValidParsingLine() {
    // Absolute coordinates
    VectorCommand lineToCommand = VectorCommand.createVectorCommand("L1,2");
    Assert.assertTrue(lineToCommand instanceof VectorCommand.LineToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.ABSOLUTE, lineToCommand.mArgFormat);
    Assert.assertEquals(2, lineToCommand.mArgs.length);
    Assert.assertEquals(1, lineToCommand.mArgs[0], 0);
    Assert.assertEquals(2, lineToCommand.mArgs[1], 0);

    // Relative coordinates
    lineToCommand = VectorCommand.createVectorCommand("l3,4");
    Assert.assertTrue(lineToCommand instanceof VectorCommand.LineToCommand);
    Assert.assertEquals(VectorCommand.ArgFormat.RELATIVE, lineToCommand.mArgFormat);
    Assert.assertEquals(2, lineToCommand.mArgs.length);
    Assert.assertEquals(3, lineToCommand.mArgs[0], 0);
    Assert.assertEquals(4, lineToCommand.mArgs[1], 0);
  }

  @Test
  public void testInvalidParsingLine() {
    // Too few arguments
    try {
      VectorCommand.createVectorCommand("L1");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires two arguments"));
    }

    // Too many arguments
    try {
      VectorCommand.createVectorCommand("L1,2,3");
      Assert.fail("Expected exception not thrown.");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      Assert.assertTrue(e.getMessage().contains("requires two arguments"));
    }
  }

  @Test
  public void testSimplePathApplication() {
    Path mockPath = Mockito.mock(Path.class);
    InOrder orderedMock = Mockito.inOrder(mockPath);

    // Absolute commands
    // MoveTo command
    VectorCommand.createVectorCommand("M0,5").apply(mockPath);
    orderedMock.verify(mockPath).moveTo(0, 5);
    // LineTo command
    VectorCommand.createVectorCommand("L10,15").apply(mockPath);
    orderedMock.verify(mockPath).lineTo(10, 15);
    // QuadraticTo command
    VectorCommand.createVectorCommand("Q20,25,30,35").apply(mockPath);
    orderedMock.verify(mockPath).quadTo(20, 25, 30, 35);
    // CubicTo command
    VectorCommand.createVectorCommand("C40,45,50,55,60,65").apply(mockPath);
    orderedMock.verify(mockPath).cubicTo(40, 45, 50, 55, 60, 65);

    // Relative commands
    // MoveTo command
    VectorCommand.createVectorCommand("m0,5").apply(mockPath);
    orderedMock.verify(mockPath).rMoveTo(0, 5);
    // LineTo command
    VectorCommand.createVectorCommand("l10,15").apply(mockPath);
    orderedMock.verify(mockPath).rLineTo(10, 15);
    // QuadraticTo command
    VectorCommand.createVectorCommand("q20,25,30,35").apply(mockPath);
    orderedMock.verify(mockPath).rQuadTo(20, 25, 30, 35);
    // CubicTo command
    VectorCommand.createVectorCommand("c40,45,50,55,60,65").apply(mockPath);
    orderedMock.verify(mockPath).rCubicTo(40, 45, 50, 55, 60, 65);
  }
}
