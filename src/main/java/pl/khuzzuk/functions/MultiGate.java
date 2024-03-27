package pl.khuzzuk.functions;

import java.util.function.Consumer;

public class MultiGate {
    public static final Runnable EMPTY_ACTION = () -> {/*empty action*/};
    private GateElement[] elements;
    private Runnable on;
    private Runnable off;
    private Consumer<GateElement> onAction;
    private Consumer<GateElement> offAction;

    private MultiGate() {
    }

    public static MultiGate of(int switches, Runnable whenOn) {
        return of(switches, whenOn, EMPTY_ACTION, true);
    }

    public static MultiGate of(int switches, Runnable whenOn, Runnable whenOff) {
        return of(switches, whenOn, whenOff, true);
    }

    public static MultiGate of(int switches, Runnable whenOn, Runnable whenOff, boolean repeatable) {
        if (switches < 2) throw new IllegalArgumentException();
        MultiGate multiGate = new MultiGate();
        multiGate.onAction = GateElement::get;
        multiGate.offAction = elements -> multiGate.off.run();
        multiGate.on = whenOn;
        multiGate.off = whenOff;
        multiGate.elements = new GateElement[switches];
        multiGate.setElements(repeatable);
        return multiGate;
    }

    private void setElements(boolean repeatable) {
        GateElement firstGate = new GateElement();
        firstGate.action = offAction;
        elements[0] = firstGate;
        int size = elements.length;
        for (int i = 1; i < size - 1; i++) {
            GateElement gate = new GateElement();
            gate.action = offAction;
            elements[i - 1].next = gate;
            elements[i] = gate;
        }
        LastGate lastGate = repeatable ? new RepeatableLastGate() : new LastGate();
        elements[size - 2].next = lastGate;
        elements[size - 1] = lastGate;
    }

    public void on(int element) {
        elements[element].on();
        elements[0].get();
    }

    public void off(int element) {
        elements[element].off();
        elements[0].get();
    }

    public int size() {
        return elements.length;
    }

    private class GateElement {
        GateElement next;
        Consumer<GateElement> action;

        private GateElement() {
            action = offAction;
        }

        void get() {
            action.accept(next);
        }

        private void off() {
            this.action = offAction;
        }

        void on() {
            this.action = onAction;
        }
    }

    private class LastGate extends GateElement {
        Consumer<GateElement> lastOnAction;

        LastGate()
        {
            lastOnAction = element -> {
                on.run();
                on = EMPTY_ACTION;
                off = EMPTY_ACTION;
            };
        }

        @Override
        void get() {
            action.accept(null);
        }

        @Override
        void on() {
            this.action = lastOnAction;
        }
    }

    private class RepeatableLastGate extends LastGate {
        RepeatableLastGate()
        {
            lastOnAction = element -> on.run();
        }
    }
}
