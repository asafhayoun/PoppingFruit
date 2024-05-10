package me.googroup.poppingfruit.draw;

import android.opengl.GLES20;

public class Shader {
  public final String source;
  public int id;
  public final int type;

  public Shader(int type, String source) {
    this.source = source;
    this.type = type;
  }

  public Shader load() {
    id = GLES20.glCreateShader(type);
    GLES20.glShaderSource(id, source);
    GLES20.glCompileShader(id);
    return this;
  }
  public void unload() {
    GLES20.glDeleteShader(id);
  }
}
