package game.entity.cell;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import com.google.gson.Gson;
import game.Game;
import game.entity.Entity;
import game.entity.food.Food;

import utils.generator.Generate;
import utils.logger.Logger;

public abstract class Cell extends Entity implements Runnable {
    public static final class Config {
        public final float eatChance; // 0.005 - 0.03
        public final float reproduceChance; // 0.005 - 0.03

        public final Integer foodPerReproduce; // 5 - 15

        public final Integer timeStarve; // 2 - 5
        public final Integer timeFull; // 2 - 8

        public Config(
                float eatChance,
                float reproduceChance,
                Integer foodPerReproduce,
                Integer timeStarve,
                Integer timeFull) {
            this.eatChance = eatChance;
            this.reproduceChance = reproduceChance;

            this.foodPerReproduce = foodPerReproduce;

            this.timeStarve = timeStarve;
            this.timeFull = timeFull;
        }

        public static Config random() {
            return new Config(
                    Generate.randomFloat(0.005f, 0.03f),
                    Generate.randomFloat(0.005f, 0.03f),
                    Generate.randomInt(5, 15),
                    Generate.randomInt(2, 5),
                    Generate.randomInt(2, 8));
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
        game.client.send("starve", this.getId().toString());
        Logger.log(this + " is starving");
        state = State.STARVING;
        restartStopwatch();
    }

    private void satiateCell() {
        game.client.send("satiate", this.getId().toString());
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
        game.client.send("eat", this.getId().toString());
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
        try {
            reproduce();

        }catch(Exception e) {
            System.out.println(e);
        }
    }

    protected void die() {
        game.killCell(this);
        game.client.send("die", this.getId().toString());
    }

    @Override
    public void run() {
        game.client.send("spawn",
                this.getId().toString(),
                config.foodPerReproduce.toString(),
                config.timeFull.toString(),
                config.timeStarve.toString());
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

    public Config getConfig() {
        return config;
    }
    public int getFoodConsumed() {
        return foodConsumed;
    }

    public void printDetails() {
        System.out.println("  - Eat chance: " + String.format("%.3f", config.eatChance));
        System.out.println("  - Reproduce chance: " + String.format("%.3f", config.reproduceChance));
        System.out.println("  - Food per reproduce: " + config.foodPerReproduce);
        System.out.println("  - Time starve: " + config.timeStarve);
        System.out.println("  - Time full: " + config.timeFull);
    }
}
