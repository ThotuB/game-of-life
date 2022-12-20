package game.statistics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Statistics {
    private static final String QUEUE_NAME = "GAME";

    class GameStats {
        public int numCells = 0;
        public int numFood = 0;
        public int numSexuateCells = 0;
        public int numAsexuateCells = 0;
    }

    private GameStats gameStats = new GameStats();

    public Statistics() {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        System.out.println(" [*] Waiting game to start...");

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            DeliverCallback callback = ((consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String type = message.split(":")[0];

                if (type.equals("spawn")) {
                    gameStats.numCells++;
                } else if (type.equals("die")) {
                    gameStats.numCells--;
                } else if (type.equals("SEXUATE_CELL")) {
                    gameStats.numSexuateCells++;
                } else if (type.equals("ASEXUATE_CELL")) {
                    gameStats.numAsexuateCells++;
                }

                printStatistics();
            });

            while (true) {
                channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearConsole() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    }

    private void printStatistics() {
        String result = "Statistics\n"
                + "==========\n"
                + "Cells: " + gameStats.numCells + "\n"
                + "Food: " + gameStats.numFood + "\n"
                + "Sexuate Cells: " + gameStats.numSexuateCells + "\n"
                + "Asexuate Cells: " + gameStats.numAsexuateCells + "\n";

        try {
            clearConsole();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(result);
    }
}
