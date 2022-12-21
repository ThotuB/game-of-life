package game.statistics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class Statistics {
    private static final String QUEUE_NAME = "GAME";
    private boolean running = true;

    class CellStats {
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

    class GameStats {
        public int numCells = 0;
        public int numFood = 0;
        public int numSexuateCells = 0;
        public int numAsexuateCells = 0;

        public HashMap<Integer, CellStats> cellStats = new HashMap<>();
    }

    private GameStats gameStats = new GameStats();

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
            // processMessage(new String(delivery.getBody(), StandardCharsets.UTF_8));
            processMessageJSON(new String(delivery.getBody(), StandardCharsets.UTF_8));
        });

        while (running) {
            channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
            });
        }
    }

    private void processMessageJSON(String jsonString) {

        JSONObject obj = new JSONObject(jsonString);
        String type = obj.getString("type");

        switch (type) {
            case "spawn" -> {
                JSONObject cell = obj.getJSONObject("cell");
                int cellId = cell.getInt("id");
                JSONObject config = cell.getJSONObject("config");
                int fpr = config.getInt("fpr");
                int tFull = config.getInt("time_full");
                int tStarve = config.getInt("time_starve");
                boolean sexuate = cell.getBoolean("type");

                gameStats.numCells++;
                gameStats.cellStats.put(cellId, new CellStats(cellId, fpr, tFull, tStarve, sexuate));
            }
            case "eat" -> {
                int cellId = obj.getInt("cell_id");
                gameStats.cellStats.get(cellId).foodEaten++;
            }
            case "reproduce-sexuate" -> {
                int cell1Id = obj.getInt("cell_1_id");
                int cell2Id = obj.getInt("cell_2_id");
                gameStats.cellStats.get(cell1Id).numChildren++;
                gameStats.cellStats.get(cell2Id).numChildren++;
            }
            case "reproduce-asexuate" -> {
                int cellId = obj.getInt("cell_id");
                gameStats.cellStats.get(cellId).numChildren++;
            }
            case "die" -> {
                int cellId = obj.getInt("cell_id");
                gameStats.numCells--;
                gameStats.cellStats.get(cellId).state = CellStats.State.DEAD;
            }
            case "satiate" -> {
                int cellId = obj.getInt("cell_id");
                gameStats.cellStats.get(cellId).state = CellStats.State.FULL;
            }
            case "starve" -> {
                int cellId = obj.getInt("cell_id");
                gameStats.cellStats.get(cellId).state = CellStats.State.STARVING;
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

    public void printStatistics() {
        String result = "STATISTICS\n"
                + "===========================================================================\n"
                + "         CELLS: " + gameStats.numCells + "\n"
                + "          FOOD: " + gameStats.numFood + "\n"
                + " SEXUATE CELLS: " + gameStats.numSexuateCells + "\n"
                + "ASEXUATE CELLS: " + gameStats.numAsexuateCells + "\n"
                + "===========================================================================\n"
                + "|  ID  |   TYPE   | FPR | T_FULL | T_STARVE |   STATE   | FOOD | CHILDREN |\n"
                + "---------------------------------------------------------------------------\n";

        for (CellStats cell : gameStats.cellStats.values()) {
            result += String.format("| %4d | %8s | %3d | %6d | %8d | %9s | %4d | %8d |\n",
                    cell.id, cell.type, cell.fpr, cell.tFull, cell.tStarve, cell.state, cell.foodEaten,
                    cell.numChildren);
        }

        result += "===========================================================================";

        try {
            clearConsole();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println(result);
    }
}
