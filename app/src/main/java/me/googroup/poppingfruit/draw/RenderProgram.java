package me.googroup.poppingfruit.draw;

import android.opengl.GLES20;

public class RenderProgram {
  public final int id;
  public RenderProgram(Shader vertexShader, Shader fragmentShader) {
    assert(vertexShader.type == GLES20.GL_VERTEX_SHADER);
    assert(fragmentShader.type == GLES20.GL_FRAGMENT_SHADER);
    id = GLES20.glCreateProgram();
    this.vertexShader = vertexShader;
    this.fragmentShader = fragmentShader;
    GLES20.glAttachShader(id, vertexShader.id);
    GLES20.glAttachShader(id, fragmentShader.id);
    GLES20.glLinkProgram(id);
  }
  public void unload() {
    GLES20.glDeleteProgram(id);
  }
  public Shader vertexShader;
  public Shader fragmentShader;
}
