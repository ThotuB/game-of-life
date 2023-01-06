package game;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import event.factory.EventFactory;
import game.client.Client;
import game.entity.cell.AsexuateCell;
import game.entity.cell.Cell;
import game.entity.cell.SexuateCell;
import game.entity.food.Food;
import utils.colors.Colors;
import utils.logger.Logger;
import utils.mating_queue.MatingQueue;

public class Game {
    public static final int TICK = 50;

    public record Config(int numSexuateCells, int numAsexuateCells, int numFood) {
    }

    private final ArrayList<Cell> cells;
    private final ArrayList<Food> foods;

    public final Client client = new Client();

    // Concurrency
    private final MatingQueue matingQueue = new MatingQueue(this);

    ExecutorService executor = Executors.newCachedThreadPool();
    private final Lock cellLock = new ReentrantLock();
    private final Lock foodLock = new ReentrantLock();

    public Game(Config config) {
        // Create cells
        this.cells = new ArrayList<Cell>(config.numSexuateCells + config.numAsexuateCells);

        for (int i = 0; i < config.numSexuateCells; i++) {
            cells.add(new SexuateCell(this, Cell.Config.random(), matingQueue));
        }

        for (int i = 0; i < config.numAsexuateCells; i++) {
            cells.add(new AsexuateCell(this, Cell.Config.random()));
        }

        // Create food
        this.foods = new ArrayList<Food>(config.numFood);

        for (int i = 0; i < config.numFood; i++) {
            foods.add(new Food());
        }

        client.send(EventFactory.gameStartedEvent(
                config.numSexuateCells,
                config.numAsexuateCells,
                config.numFood));

    }

    public Food eat() {
        foodLock.lock();
        try {
            if (foods.isEmpty()) {
                return null;
            }

            return foods.remove(getRandomFood());
        } catch (Exception e) {
            return null;
        } finally {
            foodLock.unlock();
        }
    }

    // region Cell Lifecycle Methods
    public void spawnCell(Cell cell) {
        cellLock.lock();
        try {
            cells.add(cell);

            executor.execute(cell);
        } finally {
            cellLock.unlock();
        }
    }

    public void killCell(Cell cell) {
        // Remove cell from list
        int numFoodSpawn = (int) (Math.random() * 4 + 1);

        cellLock.lock();
        try {
            cells.remove(cell);
            client.send(EventFactory.cellDiedEvent(cell, numFoodSpawn));
            Logger.log(cell + " died (" + Colors.R + cells.size() + " left" + Colors.X + ") -> +" + numFoodSpawn + " food");

        } finally {
            cellLock.unlock();
        }

        // Stop simulation if no cells left
        if (cells.isEmpty()) {
            executor.shutdown();
            cell.printDetails();

            client.send(EventFactory.createEvent("exit"));
            System.exit(0);
            return;
        }

        // Spawn food
        foodLock.lock();
        try {
            for (int i = 0; i < numFoodSpawn; i++) {
                foods.add(new Food());
            }
        } finally {
            foodLock.unlock();
        }
    }
    // endregion

    public void simulate() {
        System.out.println("--- Simulation ---");

        // start mating queue
        executor.execute(matingQueue);

        // start cells
        for (Cell cell : cells) {
            executor.execute(cell);
        }
    }

    public int getRandomFood() {
        return (int) (Math.random() * foods.size());
    }

    @Override
    public String toString() {
        return "Game:"
                + "\n  cells=" + cells
                + "\n  foods=" + foods;
    }
}
