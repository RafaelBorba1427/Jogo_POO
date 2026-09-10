
@Deprecated
public class PointCounter {

  /** @deprecated use {@link PointSystem#reached_target_points} */
  @Deprecated
  public static final Signal<Boolean> reached_target_points = PointSystem.reached_target_points;

  private PointCounter() {
  }

  /** @deprecated use {@link PointSystem#addPoints(int)} */
  @Deprecated
  public static void addPoints(int points) {
    PointSystem.addPoints(points);
  }

  /** @deprecated use {@link PointSystem#getPoints()} */
  @Deprecated
  public static int getPoints() {
    return (int) PointSystem.getDisplayPoints();
  }

  /** @deprecated use {@link PointSystem#setPoints(long)} */
  @Deprecated
  public static void setPoints(int points) {
    PointSystem.setPoints(points);
  }

  /** @deprecated use {@link PointSystem#setTargetPoints(long)} */
  @Deprecated
  public static void setTargetPoints(int target_points) {
    PointSystem.setTargetPoints(target_points);
  }

  /** @deprecated use {@link PointSystem#getTargetPoints()} */
  @Deprecated
  public static int getTargetPoints() {
    return (int) PointSystem.getTargetPoints();
  }
}
