package game.statistics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import event.*;
import org.json.JSONObject;
import utils.colors.Colors;

public class Statistics {
    private static final String QUEUE_NAME = "GAME";
    private boolean running = true;

    static class CellStats {
        enum State {
            FULL, STARVING, DEAD
        }

        enum Type {
            SEXUATE, ASEXUATE
        }

        public final int id;
        public final int fpr;
        public final int tFull;
        public final int tStarve;

        public int foodEaten = 0;
        public int numChildren = 0;
        public State state = State.FULL;
        public Type type = Type.SEXUATE;

        public CellStats(int id, int fpr, int tFull, int tStarve, boolean sexuate) {
            this.id = id;
            this.fpr = fpr;
            this.tFull = tFull;
            this.tStarve = tStarve;
            if (!sexuate)
                this.type = Type.ASEXUATE;
        }
    }

    static class GameStats {
        public int numCells;
        public int numFood;
        public int numSexuateCells;
        public int numAsexuateCells;

        public GameStats(int numSexuateCells, int numAsexuateCells, int numFood, int numCells) {
            this.numSexuateCells = numSexuateCells;
            this.numAsexuateCells = numAsexuateCells;
            this.numFood = numFood;
            this.numCells = numCells;
        }

        public final HashMap<Integer, CellStats> cellStats = new HashMap<>();
    }

    private GameStats gameStats;

    public Statistics() {
    }

    public void setup() {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            callbackLoop(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callbackLoop(Channel channel) throws IOException {
        DeliverCallback callback = ((consumerTag, delivery) -> {
            processMessage(new String(delivery.getBody(), StandardCharsets.UTF_8));
        });

        while (running) {
            channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
            });
        }
    }

    private void processMessage(String event) {
        Gson gson = new Gson();
        String type = new JSONObject(event).getString("type");

        switch (type) {
            case "game-started" -> {
                GameStartedDto eventObj = gson.fromJson(event, GameStartedDto.class);

                gameStats = new GameStats(
                        eventObj.num_sexuate(),
                        eventObj.num_asexuate(),
                        eventObj.num_food(),
                        eventObj.num_sexuate() + eventObj.num_asexuate());
            }
            case "spawn" -> {
                SpawnDto eventObj = gson.fromJson(event, SpawnDto.class);

                gameStats.cellStats.put(
                        eventObj.cell().id(),
                        new CellStats(
                                eventObj.cell().id(),
                                eventObj.cell().config().fpr(),
                                eventObj.cell().config().time_full(),
                                eventObj.cell().config().time_starve(),
                                eventObj.cell().type()));
            }
            case "eat" -> {
                CellDto eventObj = gson.fromJson(event, CellDto.class);
                gameStats.cellStats.get(eventObj.cell_id()).foodEaten++;
                gameStats.cellStats.get(eventObj.cell_id()).state = CellStats.State.FULL;
                gameStats.numFood--;
            }
            case "reproduce-sexuate" -> {
                ReproduceSexuateDto eventObj = gson.fromJson(event, ReproduceSexuateDto.class);
                gameStats.cellStats.get(eventObj.cell_1_id()).numChildren++;
                gameStats.cellStats.get(eventObj.cell_2_id()).numChildren++;
                gameStats.numSexuateCells++;
                gameStats.numCells++;
            }
            case "reproduce-asexuate" -> {
                ReproduceAsexuateDto eventObj = gson.fromJson(event, ReproduceAsexuateDto.class);
                gameStats.cellStats.get(eventObj.cell_id()).numChildren++;
                gameStats.numAsexuateCells++;
                gameStats.numCells++;
            }
            case "cell-died" -> {
                CellDiedDto eventObj = gson.fromJson(event, CellDiedDto.class);

                gameStats.numCells--;
                gameStats.cellStats.get(eventObj.cell_id()).state = CellStats.State.DEAD;
                if (eventObj.cell_type()) {
                    gameStats.numSexuateCells--;
                } else {
                    gameStats.numAsexuateCells--;
                }
                gameStats.numFood += eventObj.created_food();
            }
            case "starve" -> {
                CellDto eventObj = gson.fromJson(event, CellDto.class);
                gameStats.cellStats.get(eventObj.cell_id()).state = CellStats.State.STARVING;
            }
            case "exit" -> running = false;
            default -> {
                return;
            }
        }
        printStatistics();
    }

    private static void clearConsole() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    }

    private String printCellStatistics(CellStats cell) {
        return String.format("| %4d | %s%8s%s | %3d | %6d | %8d | %s%9s%s | %4d | %8d |\n",
                cell.id,
                switch (cell.type) {
                    case SEXUATE -> Colors.C;
                    case ASEXUATE -> Colors.B;
                },
                cell.type,
                Colors.X,
                cell.fpr,
                cell.tFull,
                cell.tStarve,
                switch (cell.state) {
                    case FULL -> Colors.G;
                    case STARVING -> Colors.Y;
                    case DEAD -> Colors.R;
                },
                cell.state,
                Colors.X,
                cell.foodEaten,
                cell.numChildren);
    }

    private void printStatistics() {
        StringBuilder result = new StringBuilder("STATISTICS\n"
                + "===========================================================================\n"
                + "         CELLS: " + gameStats.numCells + "\n"
                + "          FOOD: " + gameStats.numFood + "\n"
                + " SEXUATE CELLS: " + gameStats.numSexuateCells + "\n"
                + "ASEXUATE CELLS: " + gameStats.numAsexuateCells + "\n"
                + "===========================================================================\n"
                + "|  ID  |   TYPE   | FPR | T_FULL | T_STARVE |   STATE   | FOOD | CHILDREN |\n"
                + "---------------------------------------------------------------------------\n");

        for (CellStats cell : gameStats.cellStats.values()) {
            result.append(printCellStatistics(cell));
        }

        result.append("===========================================================================");

        try {
            clearConsole();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println(result);
    }
}
