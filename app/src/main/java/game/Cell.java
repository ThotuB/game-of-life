package game;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public abstract class Cell extends Entity implements Runnable {
    public static final class Config {
        public final float eatChance; // 0.005 - 0.03
        public final float reproduceChance; // 0.005 - 0.03

        public final int foodPerReproduce; // 5 - 15

        public final int timeStarve; // 2 - 5
        public final int timeFull; // 2 - 8


        public Config(
                float eatChance,
                float reproduceChance,
                int foodPerReproduce,
                int timeStarve,
                int timeFull) {
            this.eatChance = eatChance;
            this.reproduceChance = reproduceChance;

            this.foodPerReproduce = foodPerReproduce;

            this.timeStarve = timeStarve;
            this.timeFull = timeFull;
        }

        public static Config random() {
            float eatChance = Generate.randomFloat(0.005f, 0.03f);
            float reproduceChance = Generate.randomFloat(0.005f, 0.03f);

            int foodPerReproduce = Generate.randomInt(5, 15);

            int timeStarve = Generate.randomInt(2, 5);
            int timeFull = Generate.randomInt(2, 8);

            return new Config(eatChance, reproduceChance, foodPerReproduce, timeStarve, timeFull);
        }
    }

    protected final Game game;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private final Stopwatch tick = Stopwatch.createUnstarted();
    private int foodConsumed = 0;

    protected final Config config;

    public enum State {
        FULL, STARVING, DEAD
    }

    protected State state;

    // Constructor
    public Cell(Game game, Config config) {
        super();
        this.game = game;
        this.config = config;
        this.state = State.FULL;
    }

    public Cell(Game game, Config config, State state) {
        super();
        this.game = game;
        this.config = config;
        this.state = state;
    }

    private void restartStopwatch() {
        stopwatch.reset();
        stopwatch.start();
    }

    private void restartTick() {
        tick.reset();
        tick.start();
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
        return foodConsumed >= config.foodPerReproduce;
    }

    private void eat() {
        Food food = game.eat(this);

        if (food == null)
            return;

        foodConsumed++;
        Logger.log(this + " ate " + food
            + " (\u001B[32m" + foodConsumed + "\u001B[0m /"
            + " \u001B[32m" + config.foodPerReproduce + "\u001B[0m)");

        satiateCell();
    }

    private void tryEat() {
        float roll = (float) Math.random();

        if (roll > config.eatChance)
            return;

        eat();
    }

    protected void reproduce() {
        foodConsumed = 0;
        starveCell();
    }

    private void tryReproduce() {
        if (!canReproduce())
            return;

        float roll = (float) Math.random();

        if (roll > config.reproduceChance)
            return;

        reproduce();
    }

    protected void die() {
        game.killCell(this);
    }

    @Override
    public void run() {
        Logger.log(this + " is living");

        stopwatch.start();
        tick.start();

        while (true) {
            // Handle cell state
            switch (state) {
                case FULL:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= config.timeFull) {
                        starveCell();
                    }
                    break;
                case STARVING:
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= config.timeStarve) {
                        state = State.DEAD;
                    }
                    break;
                case DEAD:
                    die();
                    return;
            }

            // Handle cell actions
            if (tick.elapsed(TimeUnit.MILLISECONDS) >= 100) {
                tryEat();
                tryReproduce();
                restartTick();
            }
        }
    }

    @Override
    public String toString() {
        return "Cell #" + id;
    }

    public void printDetails() {
        System.out.println("  - Eat chance: " + String.format("%.3f", config.eatChance));
        System.out.println("  - Reproduce chance: " + String.format("%.3f", config.reproduceChance));
        System.out.println("  - Food per reproduce: " + config.foodPerReproduce);
        System.out.println("  - Time starve: " + config.timeStarve);
        System.out.println("  - Time full: " + config.timeFull);
    }
}

class SexuateCell extends Cell {
    private MatingQueue matingQueue;

    public SexuateCell(Game game, Config config, MatingQueue matingQueue) {
        super(game, config);
        this.matingQueue = matingQueue;
    }

    public SexuateCell(Game game, Config config, MatingQueue matingQueue, State state) {
        super(game, config, state);
        this.matingQueue = matingQueue;
    }

    @Override
    public void reproduce() {
        SexuateCell partner = matingQueue.findPartner(this);

        if (partner == null) {
            Logger.log(this + " waiting for a partner");
            return;
        }
        if (partner == this) {
            return;
        }

        var child = new SexuateCell(game, Config.random(), matingQueue, State.STARVING);

        Logger.log(this + " and " + partner + " reproducing -> " + child);
        game.spawnCell(child);

        super.reproduce();
    }

    @Override
    protected void die() {
        super.die();
        matingQueue.tryRemove(this);
    }

    @Override
    public void printDetails() {
        System.out.println("SexuateCell #" + id);
        super.printDetails();
    }
}

class AsexuateCell extends Cell {
    public AsexuateCell(Game game, Config config) {
        super(game, config);
    }

    public AsexuateCell(Game game, Config config, State state) {
        super(game, config, state);
    }

    @Override
    public void reproduce() {
        var cell = new AsexuateCell(game, Config.random(), State.STARVING);

        Logger.log(this + " is dividing -> " + cell);
        game.spawnCell(cell);

        super.reproduce();
    }

    @Override
    public void printDetails() {
        System.out.println("AsexuateCell #" + id);
        super.printDetails();
    }
}