package me.googroup.poppingfruit.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
public class Rectangle {
  public float x, y, width, height;
  private final FloatBuffer vertexBuffer;
  private final ShortBuffer drawListBuffer;

  public RenderProgram program;
  public Color color;

  static final int COORDS_PER_VERTEX = 3;
  static final int vertexStride = 12;
  static final int vertexCount = 4;

  public float[] getSquareCoords() {
    return new float[]{
      x, y + height, 0.0f,   // top left
      x, y, 0.0f,   // bottom left
      x + width, y, 0.0f,   // bottom right
      x + width, y + height, 0.0f // top right
    };
  }

  private static final short[] drawOrder = {0, 1, 2, 0, 2, 3};

  public Rectangle(float x, float y, float width, float height,
                RenderProgram program, Color clr) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    if (clr != null) color = new Color(clr.r, clr.g, clr.b, clr.a);
    else color = new Color(0.63671875f, 0.76953125f, 0.22265625f, 1.0f);
    this.program = program;

    ByteBuffer bb = ByteBuffer.allocateDirect(
      12 * 4);
    bb.order(ByteOrder.nativeOrder());
    vertexBuffer = bb.asFloatBuffer();
    vertexBuffer.put(getSquareCoords());
    vertexBuffer.position(0);

    ByteBuffer dlb = ByteBuffer.allocateDirect(
      drawOrder.length * 2);
    dlb.order(ByteOrder.nativeOrder());
    drawListBuffer = dlb.asShortBuffer();
    drawListBuffer.put(drawOrder);
    drawListBuffer.position(0);
  }

  public void updatePosition() {
    vertexBuffer.put(getSquareCoords());
    vertexBuffer.position(0);
  }

  public void draw() {
    GLES20.glUseProgram(program.id);
    int positionHandle = GLES20.glGetAttribLocation(program.id, "vPosition");
    GLES20.glEnableVertexAttribArray(positionHandle);
    GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
      GLES20.GL_FLOAT, false,
      vertexStride, vertexBuffer);
    int colorHandle = GLES20.glGetUniformLocation(program.id, "vColor");
    GLES20.glUniform4fv(colorHandle, 1, color.toArray(), 0);
    GLES20.glDrawElements(
      GLES20.GL_TRIANGLES, drawOrder.length,
      GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    GLES20.glDisableVertexAttribArray(positionHandle);
  }
  public static final Shader vertexShader = new Shader(GLES20.GL_VERTEX_SHADER,
    "attribute vec4 vPosition;" +
      "void main() {" +
      "  gl_Position = vPosition;" +
      "}");

  public static final Shader fragmentShader = new Shader(GLES20.GL_FRAGMENT_SHADER,
    "precision mediump float;" +
      "uniform vec4 vColor;" +
      "void main() {" +
      "  gl_FragColor = vColor;" +
      "}");
}