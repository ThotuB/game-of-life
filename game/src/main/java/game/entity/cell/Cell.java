package game.entity.cell;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import event.factory.EventFactory;
import game.Game;
import game.entity.Entity;
import game.entity.food.Food;

import utils.colors.Colors;
import utils.generator.Generate;
import utils.logger.Logger;

public abstract class Cell extends Entity implements Runnable {
    /**
     * @param eatChance        0.005 - 0.03
     * @param reproduceChance  0.005 - 0.03
     * @param foodPerReproduce 5 - 15
     * @param timeStarve       2 - 5
     * @param timeFull         2 - 8
     */
    public record Config(float eatChance, float reproduceChance, Integer foodPerReproduce, Integer timeStarve,
                         Integer timeFull) {

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
        Logger.log(this + " is starving");
        state = State.STARVING;
        restartStopwatch();
        game.client.send(EventFactory.createCellEvent("starve", this));
    }

    private void satiateCell() {
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
        game.client.send(EventFactory.createCellEvent("eat", this));
        Logger.log(this + " ate " + food
                + " (" + Colors.G + foodConsumed + Colors.X
                + " / " + Colors.G + config.foodPerReproduce + Colors.X + ")");

        satiateCell();
    }

    private void tryEat() {
        float roll = (float) Math.random();

        if (roll > config.eatChance)
            return;

        eat();
    }

    protected void reproduce() {
        foodConsumed -=  config.foodPerReproduce;
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
        game.client.send(EventFactory.createSpawnEvent(this));
        Logger.log(this + " is living");

        stopwatch.start();
        tick.start();

        while (true) {
            // Handle cell state
            switch (state) {
                case FULL -> {
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= config.timeFull) {
                        starveCell();
                    }
                }
                case STARVING -> {
                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= config.timeStarve) {
                        state = State.DEAD;
                    }
                }
                case DEAD -> {
                    die();
                    return;
                }
            }

            // Handle cell actions
            if (tick.elapsed(TimeUnit.MILLISECONDS) >= Game.TICK) {
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
