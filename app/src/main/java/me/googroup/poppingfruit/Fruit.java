package me.googroup.poppingfruit;

public class Fruit {
  public double x, y;
  public Type type;

  public Fruit(double x, double y, Type type) {
    this.x = x;
    this.y = y;
    this.type = type;
  }

  public boolean intersects(Fruit other) {
    double xDiff = x - other.x, yDiff = y - other.y;
    double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    return distance <= (radius() + other.radius()) * radiusScale;
  }

  public static enum Type {
    BLUEBERRY,
    CHERRY,
    WATERMELON;

    public Type nextFruit() {
      if (this == Type.WATERMELON) {
        return null;
      }
      return Type.values()[this.ordinal() + 1];
    }
  }

  public static final double[] radii = new double[] { 0.05, 0.1, 0.141, 0.33, 0.4, 0.5 };

  public static float radiusScale = 1;
  public double radius() {
    return radii[type.ordinal()];
  }
}
