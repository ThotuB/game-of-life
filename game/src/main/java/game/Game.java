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
import utils.logger.Logger;

public class Game {
    public static final class Config {
        public final int numSexuateCells;
        public final int numAsexuateCells;
        public final int numFood;

        public Config(
                int numSexuateCells,
                int numAsexuateCells,
                int numFood) {
            this.numSexuateCells = numSexuateCells;
            this.numAsexuateCells = numAsexuateCells;
            this.numFood = numFood;
        }
    }

    private ArrayList<Cell> cells;
    private ArrayList<Food> foods;

    public final Client client = new Client();

    // Concurrency
    //private MatingQueue matingQueue = new MatingQueue();
    private ConcurrentLinkedQueue<SexuateCell> matingQueue = new ConcurrentLinkedQueue<>();

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
    }

    public Food eat() {
        foodLock.lock();
        try {
            if (foods.isEmpty()) {
                return null;
            }

            Food food = foods.remove(getRandomFood());
            // statistics.numFood--;

            return food;
        } catch (Exception e) {
            return null;
        } finally {
            foodLock.unlock();
        }
    }

    // region Cell Count Methods
    private void decrementCellCount(Cell cell) {
        if (cell instanceof SexuateCell) {
            // statistics.numSexuateCells--;
        } else if (cell instanceof AsexuateCell) {
            // statistics.numAsexuateCells--;
        }
    }

    private void incrementCellCount(Cell cell) {
        if (cell instanceof SexuateCell) {
            // statistics.numSexuateCells++;
        } else if (cell instanceof AsexuateCell) {
            // statistics.numAsexuateCells++;
        }
    }
    // endregion

    // region Cell Lifecycle Methods
    public void spawnCell(Cell cell) {
        cellLock.lock();
        try {
            cells.add(cell);
            incrementCellCount(cell);

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
            Logger.log(cell + " died (\u001B[31m" + cells.size() + " left\u001B[0m) -> +" + numFoodSpawn + " food");
            decrementCellCount(cell);
        } finally {
            cellLock.unlock();
        }

        // Stop simulation if no cells left
        if (cells.isEmpty()) {
            executor.shutdown();
            cell.printDetails();

            client.sendJson(EventFactory.createEvent("exit"));
            return;
        }

        // Spawn food
        foodLock.lock();
        try {
            for (int i = 0; i < numFoodSpawn; i++) {
                foods.add(new Food());
                // statistics.numFood++;
            }
        } finally {
            foodLock.unlock();
        }
    }
    // endregion

    public void simulate() {
        System.out.println("--- Simulation ---");

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
