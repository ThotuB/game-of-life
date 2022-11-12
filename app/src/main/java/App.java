import game.Game;

public class App {
    public static void main(String[] args) {
        Game.Config config = new Game.Config(
            6,
            6,
            22
        );

        Game game = new Game(config);

        game.simulate();
    }
}
