package game;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Game {
    public static final class Config {
        public int timeStarve = 3;
        public int timeFull = 5;

        public int numSexuateCells = 5;
        public int numAsexualCells = 5;
        public int numFood = 40;

        public Config(int timeStarve, int timeFull, int numSexuateCells, int numAsexualCells, int numFood) {
            this.timeStarve = timeStarve;
            this.timeFull = timeFull;
            this.numSexuateCells = numSexuateCells;
            this.numAsexualCells = numAsexualCells;
            this.numFood = numFood;
        }
    }

    private final int TIME_STARVE;
    private final int TIME_FULL;

    private int numSexuateCells;
    private int numAsexuateCells;
    private ArrayList<Cell> cells;

    private int numFood;
    private ArrayList<Food> foods;

    // Concurrency
    ExecutorService executor = Executors.newCachedThreadPool();
    private final Lock cellLock = new ReentrantLock();
    private final Lock foodLock = new ReentrantLock();

    public Game(Config config) {
        this.TIME_STARVE = config.timeStarve;
        this.TIME_FULL = config.timeFull;

        // Create cells
        this.numSexuateCells = config.numSexuateCells;
        this.numAsexuateCells = config.numAsexualCells;
        this.cells = new ArrayList<Cell>(numSexuateCells + numAsexuateCells);

        for (int i = 0; i < numSexuateCells; i++) {
            cells.add(new SexuateCell(this));
        }

        for (int i = 0; i < numAsexuateCells; i++) {
            cells.add(new AsexuateCell(this));
        }

        // Create food
        this.numFood = config.numFood;
        this.foods = new ArrayList<Food>(numFood);

        for (int i = 0; i < numFood; i++) {
            foods.add(new Food());
        }
    }

    public boolean eat(Cell cell, int foodId) {
        boolean ate = false;
        foodLock.lock();
        try {
            // Check if food exists
//             Optional<Food> food = this.foods.stream()
//                 .filter(f -> f.getId() == foodId)
//                 .findFirst();
// 
//             // If food exists, remove it from the list
//             food.ifPresent(f -> {
//                 this.foods.remove(f);
//             });
// 
//             if (food.isPresent()) {
//                 ate = true;
//                 numFood--;
//             }
            if (foods.size() > foodId) {
                Logger.log(cell + " ate " + foods.get(foodId));
                foods.remove(foodId);
                ate = true;
                numFood--;
            }
        }
        finally {
            foodLock.unlock();
        }

        return ate;
    }

    //region Cell Count Methods
    private void decrementCellCount(Cell cell) {
        if (cell instanceof SexuateCell) {
            numSexuateCells--;
        }
        else if (cell instanceof AsexuateCell) {
            numAsexuateCells--;
        }
    }

    private void incrementCellCount(Cell cell) {
        if (cell instanceof SexuateCell) {
            numSexuateCells++;
        }
        else if (cell instanceof AsexuateCell) {
            numAsexuateCells++;
        }
    }
    //endregion

    //region Cell Lifecycle Methods
    public void spawnCell(Cell cell) {
        cellLock.lock();
        try {
            cells.add(cell);
            incrementCellCount(cell);

            executor.execute(cell);
        }
        finally {
            cellLock.unlock();
        }
    }

    public void killCell(Cell cell) {
        cellLock.lock();
        try {
            cells.remove(cell);
            Logger.log(cell + " died (\u001B[31m" + cells.size() + " left\u001B[0m)");
            decrementCellCount(cell);

            if (cells.isEmpty()) {
                executor.shutdown();
            }
        }
        finally {
            cellLock.unlock();
        }

        foodLock.lock();
        try {
            int numFoodSpawn = (int) (Math.random() * 4 + 1);
            for (int i = 0; i < numFoodSpawn; i++) {
                foods.add(new Food());
                numFood++;
            }
        }
        finally {
            foodLock.unlock();
        }
    }
    //endregion

    public void simulate() {
        System.out.println("--- Simulation ---");

        for (Cell cell : cells) {
            executor.execute(cell);
        }

        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("--- Simulation ---");
    }

    public int getTimeStarve() { return TIME_STARVE; }
    public int getTimeFull() { return TIME_FULL; }

    @Override
    public String toString() {
        return "Game:"
            + "\n  numSexuateCells=" + numSexuateCells
            + "\n  numAsexuateCells=" + numAsexuateCells
            + "\n  cells=" + cells
            + "\n  numFood=" + numFood
            + "\n  foods=" + foods;
    }
}
