@FunctionalInterface
public interface SignalListener<T> {
    void onSignal(T value);
}