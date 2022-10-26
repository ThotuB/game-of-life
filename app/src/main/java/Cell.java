import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class Cell extends Entity implements Runnable {
    private Stopwatch stopwatch = Stopwatch.createUnstarted();
    private int foodConsumed;

    public enum State {
        FULL, STARVING, DEAD
    }

    public enum Type {
        SEXUATE, ASEXUATE
    }

    private State state;
    private Type type;

    public Cell(State state, Type type) {
        super();
        this.state = state;
        this.type = type;
        this.foodConsumed = 0;
    }

    @Override
    public void run() {
        stopwatch.start();

        while (true) {
            switch (state) {
                case FULL:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= 2) {
                        toString();
                        state = State.STARVING;
                        stopwatch.reset();
                        stopwatch.start();
                    }
                    break;
                case STARVING:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= 3) {
                        toString();
                        state = State.DEAD;
                    }
                    break;
                case DEAD:
                    toString();
                    return;
            }
        }
    }

    @Override
    public String toString() {
        return "Cell #" + id + " is " + state + " and " + type;
    }
}