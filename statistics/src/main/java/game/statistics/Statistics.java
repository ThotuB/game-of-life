package game.statistics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Statistics {
    private static final String QUEUE_NAME = "GAME";

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
            processMessage(new String(delivery.getBody(), StandardCharsets.UTF_8));
        });

        while (true) {
            channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
            });
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split(":");
        String type = parts[0];
        int id = Integer.parseInt(parts[1]);

        switch (type) {
            case "spawn":
                int fpr = Integer.parseInt(parts[2]);
                int tFull = Integer.parseInt(parts[3]);
                int tStarve = Integer.parseInt(parts[4]);

                gameStats.numCells++;
                gameStats.cellStats.put(id, new CellStats(id, fpr, tFull, tStarve));
                break;
            case "die":
                gameStats.numCells--;
                gameStats.cellStats.get(id).state = CellStats.State.DEAD;
                break;
            case "eat":
                gameStats.cellStats.get(id).foodEaten++;
                break;
            case "satiate":
                gameStats.cellStats.get(id).state = CellStats.State.FULL;
                break;
            case "starve":
                gameStats.cellStats.get(id).state = CellStats.State.STARVING;
                break;
            case "reproduce":
                gameStats.cellStats.get(id).numChildren++;
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
            return;
        }

        System.out.println(result);
    }
}
