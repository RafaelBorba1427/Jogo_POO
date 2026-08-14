import java.util.ArrayList;
import java.util.List;

public class Signal<T> {

    private final List<SignalListener<T>> listeners = new ArrayList<>();

    public void connect(SignalListener<T> listener) {
        listeners.add(listener);
    }

    public void disconnect(SignalListener<T> listener) {
        listeners.remove(listener);
    }

    public void emit(T value) {
        for (SignalListener<T> listener : List.copyOf(listeners)) {
            listener.onSignal(value);
        }
    }
}