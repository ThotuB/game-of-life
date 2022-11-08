package game;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Stopwatch;

public abstract class Cell extends Entity implements Runnable {
    protected Game game;
    private Stopwatch stopwatch = Stopwatch.createUnstarted();
    private int foodConsumed = 0;

    public enum State {
        FULL, STARVING, DEAD
    }

    protected State state;

    // Constructor
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

    private void restartStopwatch() {
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

    private void eat() {
        Food food = game.eat(this, 0);

        if (food != null) {
            foodConsumed++;
            Logger.log(this + " ate " + food + " (\u001B[32m" + foodConsumed + "\u001B[0m)");

            satiateCell();
        }
    }

    private void tryEat() {
        float roll = (float) Math.random();

        if (roll > 1.0E-6) return;

        eat();
    }

    protected abstract boolean reproduce();

    private void tryReproduce() {
        if (!canReproduce()) return;

        float roll = (float) Math.random();

        if (roll > 1.0E-7) return;

        boolean hasReproduced = reproduce();

        if (hasReproduced) {
            foodConsumed = 0;
            starveCell();
        }
    }

    private void die() {
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
    private MatingQueue matingQueue;
    public SexuateCell(Game game, MatingQueue matingQueue) {
        super(game);
        this.matingQueue = matingQueue;
    }

    public SexuateCell(Game game, MatingQueue matingQueue, State state) {
        super(game, state);
        this.matingQueue = matingQueue;
    }

    @Override
    public boolean reproduce() {
        matingQueue.reproduceLock.lock();
        try{
            if (matingQueue.getSizeOfReproducingList() == 0) {
                Logger.log(this + " is waiting for a mate");
                if (! matingQueue.cellAlreadyExistsInReproducingList(this))
                    matingQueue.addCellToReproducingList(this);
                return false;
            }
            if (! matingQueue.cellAlreadyExistsInReproducingList(this)) {
                SexuateCell reproducedCell = matingQueue.popFirstCellFromReproducingList();
                var cell = new SexuateCell(game, matingQueue, State.STARVING);
                Logger.log(this + " has reproduced with " + reproducedCell + " -> " + cell);
                game.spawnCell(cell);
            }
        }
        finally {
            matingQueue.reproduceLock.unlock();
        }
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
        Logger.log(this + " is dividing -> " + cell);
        game.spawnCell(cell);
        return true;
    }
}