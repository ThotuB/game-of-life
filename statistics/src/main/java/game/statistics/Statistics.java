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

        public CellStats(int id, int fpr, int tFull, int tStarve) {
            this.id = id;
            this.fpr = fpr;
            this.tFull = tFull;
            this.tStarve = tStarve;
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
            //processMessage(new String(delivery.getBody(), StandardCharsets.UTF_8));
            try {
                processMessageJSON(new String(delivery.getBody(), StandardCharsets.UTF_8));
            } catch (JSONException e) {
               e.printStackTrace();
            }
        });

        while (running) {
            channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
            });
        }
    }


    private void processMessageJSON(String jsonString) throws JSONException {
        if (jsonString.charAt(0) != '{') {
            System.out.println(jsonString + " is not a valid json!");
            return;
        }

        JSONObject obj = new JSONObject(jsonString);
        String type = obj.getString("type");
        int cell_id = obj.getJSONObject("Cell1").getInt("id");

        switch (type) {
            case "sexuateReproduce":
                int cell2_id = obj.getJSONObject("Cell2").getInt("id");

                gameStats.cellStats.get(cell_id).numChildren++;
                gameStats.cellStats.get(cell2_id).numChildren++;
                break;

            case "asexuateReproduce":
                gameStats.cellStats.get(cell_id).numChildren++;
                break;

            case "spawn":
                JSONObject config = new JSONObject(obj.getJSONObject("Cell1").getString("config"));
                System.out.println("Decoded config: " + config);
                int fpr = config.getInt("foodPerReproduce");
                int tFull = config.getInt("timeFull");
                int tStarve = config.getInt("timeStarve");

                gameStats.numCells++;
                gameStats.cellStats.put(cell_id, new CellStats(cell_id, fpr, tFull, tStarve));
                break;

            case "die":
                gameStats.numCells--;
                gameStats.cellStats.get(cell_id).state = CellStats.State.DEAD;
                break;

            case "eat":
                gameStats.cellStats.get(cell_id).foodEaten++;
                break;

            case "satiate":
                gameStats.cellStats.get(cell_id).state = CellStats.State.FULL;
                break;
            case "starve":
                gameStats.cellStats.get(cell_id).state = CellStats.State.STARVING;
                break;
            case "exit":
                running = false;
                break;

            default:
                return;
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
