package pl.khuzzuk.functions;

public class Switcher {
    private final Runnable on;
    private final Runnable off;
    private Shift<Runnable> shift;

    private Switcher(Runnable on, Runnable off) {
        this.on = on;
        this.off = off;
    }

    public static Switcher get(Runnable on, Runnable off) {
        Switcher switcher = new Switcher(on == null ? () -> {} : on,
                off == null ? () -> {} : off);
        switcher.shift = Shift.get(on, off);
        return switcher;
    }

    public void off() {
        off.run();
        shift = Shift.get(on, off);
    }

    public void change() {
        shift.get().run();
    }
}
