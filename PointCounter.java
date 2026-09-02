public class PointCounter {
  private static int points;
  private static int target_points;
  public static final Signal<Boolean> reached_target_points = new Signal<>();

  public static void addPoints(int points) {
    PointCounter.points += points;
    if (PointCounter.points >= PointCounter.target_points) {
      reached_target_points.emit(true);
    }
  }

  public static int getPoints() {
    return PointCounter.points;
  }

  public static void setPoints(int points) {
    PointCounter.points = points;
  }


  public static void setTargetPoints(int target_points) {
    PointCounter.target_points = target_points;
  }

  public static int getTargetPoints() {
    return PointCounter.target_points;
  }
}
