package game;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

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

    public final class Statistics {
        public int numSexuateCells;
        public int numAsexuateCells;
        public int numFood;

        public Statistics(int numSexuateCells, int numAsexuateCells, int numFood) {
            this.numSexuateCells = numSexuateCells;
            this.numAsexuateCells = numAsexuateCells;
            this.numFood = numFood;
        }
    }

    private ArrayList<Cell> cells;
    private ArrayList<Food> foods;

    public final Statistics statistics;

    // Concurrency
    private MatingQueue matingQueue = new MatingQueue();
    ExecutorService executor = Executors.newCachedThreadPool();
    private final Lock cellLock = new ReentrantLock();
    private final Lock foodLock = new ReentrantLock();

    public Game(Config config) {
        // Statistics
        this.statistics = new Statistics(
                config.numSexuateCells,
                config.numAsexuateCells,
                config.numFood
        );

        // Create cells
        this.cells = new ArrayList<Cell>(statistics.numSexuateCells + statistics.numAsexuateCells);

        for (int i = 0; i < statistics.numSexuateCells; i++) {
            cells.add(new SexuateCell(this, Cell.Config.random(), matingQueue));
        }

        for (int i = 0; i < statistics.numAsexuateCells; i++) {
            cells.add(new AsexuateCell(this, Cell.Config.random()));
        }

        // Create food
        this.foods = new ArrayList<Food>(statistics.numFood);

        for (int i = 0; i < statistics.numFood; i++) {
            foods.add(new Food());
        }
    }

    public Food eat(Cell cell) {
        foodLock.lock();
        try {
            if (foods.isEmpty()) {
                return null;
            }

            Food food = foods.remove(getRandomFood());
            statistics.numFood--;

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
            statistics.numSexuateCells--;
        } else if (cell instanceof AsexuateCell) {
            statistics.numAsexuateCells--;
        }
    }

    private void incrementCellCount(Cell cell) {
        if (cell instanceof SexuateCell) {
            statistics.numSexuateCells++;
        } else if (cell instanceof AsexuateCell) {
            statistics.numAsexuateCells++;
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
            return;
        }

        // Spawn food
        foodLock.lock();
        try {
            for (int i = 0; i < numFoodSpawn; i++) {
                foods.add(new Food());
                statistics.numFood++;
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
                + "\n  numSexuateCells=" + statistics.numSexuateCells
                + "\n  numAsexuateCells=" + statistics.numAsexuateCells
                + "\n  cells=" + cells
                + "\n  numFood=" + statistics.numFood
                + "\n  foods=" + foods;
    }
}
