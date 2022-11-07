package game;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public abstract class Cell extends Entity implements Runnable {
    private Stopwatch stopwatch = Stopwatch.createUnstarted();
    protected Game game;
    private int foodConsumed = 0;

    public enum State {
        FULL, STARVING, DEAD
    }

    protected State state;

    public Cell(Game game) {
        super();
        this.game = game;
        this.state = State.FULL;
    }

    public Cell(Game game, State state) {
        super();
        this.game = game;
        this.state = state;
    }

    public void restartStopwatch() {
        stopwatch.reset();
        stopwatch.start();
    }

    private void starveCell() {
        Logger.log(this + " is starving");
        state = State.STARVING;
        restartStopwatch();
    }

    private void satiateCell() {
        state = State.FULL;
        restartStopwatch();
    }

    private boolean canReproduce() {
        return foodConsumed >= 10;
    }

    public void eat() {
        boolean ate = game.eat(this, 0);

        if (ate) {
            foodConsumed++;

            satiateCell();
        }
    }

    public void tryEat() {
        float roll = (float) Math.random();

        if (roll > 1.0E-7) return;

        eat();
    }

    public abstract boolean reproduce();

    public void tryReproduce() {
        if (!canReproduce()) return;

        float roll = (float) Math.random();

        if (roll > 1.0E-7) return;

        boolean hasReproduced = reproduce();

        if (hasReproduced) {
            foodConsumed = 0;
            starveCell();
        }
    }

    public void die() {
        game.killCell(this);
    }

    @Override
    public void run() {
        Logger.log(this + " is living");
        
        stopwatch.start();

        while (true) {
            // Handle cell state
            switch (state) {
                case FULL:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= game.getTimeFull()) {
                        starveCell();
                    }
                    break;
                case STARVING:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= game.getTimeStarve()) {
                        state = State.DEAD;
                    }
                    break;
                case DEAD:
                    die();
                    return;
            }

            // Handle cell actions
            tryEat();
            tryReproduce();
        }
    }

    @Override
    public String toString() {
        return "Cell #" + id;
    }
}

class SexuateCell extends Cell {
    public SexuateCell(Game game) {
        super(game);
    }

    public SexuateCell(Game game, State state) {
        super(game, state);
    }

    @Override
    public boolean reproduce() {
        var cell = new SexuateCell(game, State.STARVING);
        Logger.log(this + " is reproducing -> " + cell);
        game.spawnCell(cell);
        return true;
    }
}

class AsexuateCell extends Cell {
    public AsexuateCell(Game game) {
        super(game);
    }

    public AsexuateCell(Game game, State state) {
        super(game, state);
    }

    @Override
    public boolean reproduce() {
        var cell = new AsexuateCell(game, State.STARVING);
        Logger.log(this + " is reproducing -> " + cell);
        game.spawnCell(cell);
        return true;
    }
}