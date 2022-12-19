/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class App {
    public static void Receive() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        System.out.println("connection created");
        Channel channel = connection.createChannel();
        System.out.println("channel created");

        channel.queueDeclare("HELLO", false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback callback = ((consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        });

        channel.basicConsume("HELLO", true, callback, consumerTag -> { });

    }

    public static void main(String[] args) throws Exception {
        Receive();
    }
}
