package game.entity.cell;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import game.Game;
import game.entity.Entity;
import game.entity.food.Food;
import utils.generator.Generate;
import utils.logger.Logger;

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
        game.client.send("starve", this.toString());
        Logger.log(this + " is starving");
        state = State.STARVING;
        restartStopwatch();
    }

    private void satiateCell() {
        game.client.send("satiate", this.toString());
        state = State.FULL;
        restartStopwatch();
    }

    private boolean canReproduce() {
        return foodConsumed >= config.foodPerReproduce;
    }

    private void eat() {
        Food food = game.eat();

        if (food == null)
            return;

        foodConsumed++;
        game.client.send("eat", this.toString());
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
        game.client.send("die", this.toString());
    }

    @Override
    public void run() {
        game.client.send("spawn", this.toString());
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
            if (tick.elapsed(TimeUnit.MILLISECONDS) >= 50) {
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
