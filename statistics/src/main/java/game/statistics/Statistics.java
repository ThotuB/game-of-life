package game.statistics;

import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Statistics {
    private static final String QUEUE_NAME = "GAME";

    public Statistics() {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            DeliverCallback callback = ((consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            });

            while (true) {
                channel.basicConsume(QUEUE_NAME, true, callback, consumerTag -> {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
