package me.googroup.poppingfruit;

public class CollisionDetector {
  public static boolean circleFitsInRect(float cOffsetX, float cOffsetY, float radius, float width, float height) {
    float circleLeft = cOffsetX - radius;
    float circleRight = cOffsetX + radius;
    float circleTop = cOffsetY - radius;
    float circleBottom = cOffsetY + radius;

    return circleLeft >= 0 && circleRight <= width && circleTop >= 0 && circleBottom <= height;
  }
}
