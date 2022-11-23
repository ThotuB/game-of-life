import game.Game;

public class App {
    public static void main(String[] args) {
        Game.Config config = new Game.Config(
            10,
            10,
            100
        );

        Game game = new Game(config);

        game.simulate();
    }
}
