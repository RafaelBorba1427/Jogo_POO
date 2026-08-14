// A test class for Signals, not a part of the game

public class testSignals {
  public static void main(String[] args) {
    Signal<String> signal = new Signal<>();

    SignalListener<String> listener1 = (String value) -> {
      System.out.println("Listener 1 received: " + value);
    };

    SignalListener<String> listener2 = (String value) -> {
      System.out.println("Listener 2 received: " + value);
    };

    signal.connect(listener1);
    signal.connect(listener2);
    signal.connect(testSignals::testFunction);

    signal.emit("Hello, World!");

    signal.disconnect(listener1);

    signal.emit("Goodbye, World!");
  }

  public static void testFunction(String value) {
    System.out.println("This is a test function: " + value);
  }
}
