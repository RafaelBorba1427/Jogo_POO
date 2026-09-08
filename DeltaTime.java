import javax.swing.Timer;

public class DeltaTime {
  private Timer timer;
  private int delta_time;

  public DeltaTime() {
    delta_time = 0;
    timer = new Timer(1, e -> update());
    timer.start();
  }

  private void update(){
    if (delta_time > Integer.MAX_VALUE - 1) {
      delta_time = Integer.MAX_VALUE; // Reset to avoid overflow
    }
    delta_time++;
  }

  public int get() {
    int current_delta_time = delta_time;
    delta_time = 0; // Reset after reading
    return current_delta_time;
  }
}
