import game.Game;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class App {
    public static void Send() {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();

            channel.queueDeclare("HELLO", false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", "HELLO", null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
        catch (Exception ex){
            System.out.println(ex.toString());
        }
    }



    public static void main(String[] args) {
//        Game.Config config = new Game.Config(
//            10,
//            10,
//            100
//        );
//
//        Game game = new Game(config);
//
//        game.simulate();

        System.out.println("Starting message receiver stub");


        Send();
    }
}
