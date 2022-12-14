import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.concurrent.TimeUnit;

public class Driver
{
    private static final String QUEUE_NAME = "HELLO";
    private static int iteration = 1;
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting rabbitmq driver!");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel())
        {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            while (true)
            {
                String message = "Generated event " + iteration;
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");

                TimeUnit.SECONDS.sleep(1);
                iteration++;
            }
        }
    }
}