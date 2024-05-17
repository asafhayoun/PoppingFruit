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

public class Image {
  int quadPositionHandle, texPositionHandle, textureUniformHandle/*, viewProjectionMatrixHandle*/;
  int[] textureUnit = new int[1];

  public int getTexture() {
    return textureUnit[0];
  }

  public void setTexture(int texture) {
    textureUnit[0] = texture;
  }
  Bitmap textureBitmap;
  public final RenderProgram program;
  public static final Shader vertexShader = new Shader(GLES20.GL_VERTEX_SHADER,
      "attribute vec4 a_Position;" +
      "attribute vec2 a_TexCoord;" +
      "varying vec2 v_TexCoord;" +
      "void main() {" +
      "  gl_Position = a_Position;" +
      "  v_TexCoord = vec2(a_TexCoord.x, (1.0 - (a_TexCoord.y)));" +
      "}"
  );
  public static final Shader fragmentShader = new Shader(GLES20.GL_FRAGMENT_SHADER,
    "\n" +
      "precision mediump float;" +
      "uniform sampler2D u_Texture;" +
      "varying vec2 v_TexCoord;" +
      "void main() {" +
      "  gl_FragColor = texture2D(u_Texture, v_TexCoord);" +
      "}"
  );
  private final float[] QUADRANT_COORDINATES = new float[]{
    //x,    y
    -0.5f, 0.5f, 0f,
    -0.5f, -0.5f, 0f,
    0.5f, -0.5f, 0f,
    0.5f, 0.5f, 0f,
  };
  public void setCoords(float[] coords) {
    quadrantVertexBuffer.put(coords);
    quadrantVertexBuffer.position(0);
  }
  public Image setCoords(float x, float y, float width, float height) {
    setCoords(new float[] {
      x, y + height, 0.0f,   // top left
      x, y, 0.0f,   // bottom left
      x + width, y, 0.0f,   // bottom right
      x + width, y + height, 0.0f // top right
    });
    return this;
  }
  @NotNull
  public Offset getSecondVertexXY() {
    return new Offset(quadrantVertexBuffer.get(3), quadrantVertexBuffer.get(4));
  }
  public float[] textureCoordinates = new float[] {
    //x,    y
    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f,
    1.0f, 1.0f,
  };
  private final FloatBuffer quadrantVertexBuffer;
  private final FloatBuffer texVertexBuffer;
  private final ShortBuffer drawListBuffer;

  public Image(RenderProgram program) {
    this.program = program;
//    viewProjectionMatrixHandle = GLES20.glGetUniformLocation(program.id, "uVPMatrix");
    ByteBuffer bb = ByteBuffer.allocateDirect(
      12 * 4);
    bb.order(ByteOrder.nativeOrder());
    quadrantVertexBuffer = bb.asFloatBuffer();
    quadrantVertexBuffer.put(QUADRANT_COORDINATES);
    quadrantVertexBuffer.position(0);
    ByteBuffer txb = ByteBuffer.allocateDirect(
      8 * 4);
    txb.order(ByteOrder.nativeOrder());
    texVertexBuffer = txb.asFloatBuffer();
    texVertexBuffer.put(textureCoordinates);
    texVertexBuffer.position(0);
    ByteBuffer dlb = ByteBuffer.allocateDirect(
      drawOrder.length * 2);
    dlb.order(ByteOrder.nativeOrder());
    drawListBuffer = dlb.asShortBuffer();
    drawListBuffer.put(drawOrder);
    drawListBuffer.position(0);

    GLES20.glUseProgram(program.id);
    quadPositionHandle = GLES20.glGetAttribLocation(program.id, "a_Position");
    texPositionHandle = GLES20.glGetAttribLocation(program.id, "a_TexCoord");
    textureUniformHandle = GLES20.glGetUniformLocation(program.id, "u_Texture");
  }
  public int loadTexture(Context context, String image) throws IOException {
    textureBitmap =
      BitmapFactory.decodeStream(context.getAssets().open(image));

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glGenTextures(textureUnit.length, textureUnit, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureUnit[0]);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    textureBitmap.recycle();
    return textureUnit[0];
  }
  public void unloadTexture() {
    GLES20.glDeleteTextures(1, textureUnit, 0);
  }

  private static final short[] drawOrder = { 0, 1, 2, 0, 2, 3 };
  public void draw() {
    draw(false);
  }
  public void draw(boolean custom) {
    if(!custom) GLES20.glUseProgram(program.id);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureUnit[0]);
    GLES20.glUniform1i(textureUniformHandle, 0);
    // GLES20.glUniformMatrix4fv(viewProjectionMatrixHandle, 1, false, vPMatrix, 0);

    //Pass quadrant position to shader
    GLES20.glVertexAttribPointer(
      quadPositionHandle,
      3,
      GLES20.GL_FLOAT,
      false,
      3 * 4,
      quadrantVertexBuffer
    );

    //Pass texture position to shader
    GLES20.glVertexAttribPointer(
      texPositionHandle,
      2,
      GLES20.GL_FLOAT,
      false,
      2 * 4,
      texVertexBuffer
    );

    GLES20.glEnableVertexAttribArray(quadPositionHandle);
    GLES20.glEnableVertexAttribArray(texPositionHandle);
    GLES20.glDrawElements(
      GLES20.GL_TRIANGLES,
      drawOrder.length,
      GLES20.GL_UNSIGNED_SHORT,
      drawListBuffer
    );

    GLES20.glDisableVertexAttribArray(quadPositionHandle);
    GLES20.glDisableVertexAttribArray(texPositionHandle);
  }
}
