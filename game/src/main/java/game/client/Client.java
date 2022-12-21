package game.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

public class Client {
    private static final String QUEUE_NAME = "GAME";
    private final Channel channel;
    private final Connection connection;

    public Client() {
        System.out.println("Creating Factory...");
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            System.out.println("Creating Connection...");
            this.connection = factory.newConnection();
            System.out.println("Creating Channel...");
            this.channel = connection.createChannel();

            System.out.println("Declaring Queue...");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void send(String message) {
        //final String message = type + ":" + String.join(":", values);

        try {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void sendJson(JSONObject json)
    {
        try {
            channel.basicPublish("", QUEUE_NAME, null, json.toString().getBytes());
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