package me.googroup.poppingfruit;

import me.googroup.poppingfruit.draw.Offset;
import org.jetbrains.annotations.Nullable;

public class Fruit {
  public static enum Type {
    CHERRY
  }
  public Fruit(Type type) {
    this.type = type;
    position = new Offset(0, 0.5f);
  }
  Type type;
  /** Center */
  Offset position;
  @Nullable
  Fruit previousFruit;
  float getRadius() {
    switch (type) {
      case CHERRY:
        return 0.125f;
      default: // Java bad
        return 0;
    }
  }
}
