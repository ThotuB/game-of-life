package game.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Client {
    private static final String QUEUE_NAME = "GAME";
    private final Channel channel;
    private final Connection connection;

    public Client() {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void send(String type, String msg) {
        final String message = type + ":" + msg;

        try {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}