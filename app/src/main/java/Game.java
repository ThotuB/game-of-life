import java.util.ArrayList;
import java.util.concurrent.*;

public class Game {
    public static class Config {
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
    private ArrayList<Cell> sexuateCells;
    private int numAsexuateCells;
    private ArrayList<Cell> asexuateCells;

    private int numFood;
    private ArrayList<Food> food;

    public Game(Config config) {
        this.TIME_STARVE = config.timeStarve;
        this.TIME_FULL = config.timeFull;

        this.numSexuateCells = config.numSexuateCells;
        this.numAsexuateCells = config.numAsexualCells;
        this.numFood = config.numFood;

        this.sexuateCells = new ArrayList<Cell>();
        this.asexuateCells = new ArrayList<Cell>();
        this.food = new ArrayList<Food>();

        for (int i = 0; i < numSexuateCells; i++) {
            sexuateCells.add(new Cell(Cell.State.FULL, Cell.Type.SEXUATE));
        }

        for (int i = 0; i < numAsexuateCells; i++) {
            asexuateCells.add(new Cell(Cell.State.FULL, Cell.Type.ASEXUATE));
        }

        for (int i = 0; i < numFood; i++) {
            food.add(new Food());
        }
    }

    public void simulate() {
        ExecutorService pool = Executors.newFixedThreadPool(20);

        for (int i = 0; i < numSexuateCells; i++) {
            pool.execute(sexuateCells.get(i));
        }

        for (int i = 0; i < numAsexuateCells; i++) {
            pool.execute(asexuateCells.get(i));
        }

        pool.shutdown();
    }
}
