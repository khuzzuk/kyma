package pl.khuzzuk.functions;

public class ForceGate
{
    private int onCounter;
    private int maxCounter;
    private MultiGate multiGate;
    private ForceGate() {
    }

    public static ForceGate of(int switches, Runnable whenOn) {
        return of(switches, whenOn, MultiGate.EMPTY_ACTION, false);
    }

    public static ForceGate of(int switches, Runnable whenOn, Runnable whenOff) {
        return of(switches, whenOn, whenOff, false);
    }

    public static ForceGate of(int switches, Runnable whenOn, Runnable whenOff, boolean repeatable) {
        ForceGate forceGate = new ForceGate();
        forceGate.multiGate = MultiGate.of(switches, whenOn, whenOff, repeatable);
        forceGate.maxCounter = switches - 1;
        return forceGate;
    }

    public void on() {
        multiGate.on(onCounter);
        if (onCounter < maxCounter) onCounter++;
    }

    public void off() {
        if (onCounter > 0) onCounter--;
        multiGate.off(onCounter);
    }
}
