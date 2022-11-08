import game.Game;

public class App {
    public static void main(String[] args) {
        Game.Config config = new Game.Config(
            5,
            3,
            20,
            0,
            202
        );

        Game game = new Game(config);

        System.out.println(game);

        game.simulate();

        System.out.println(game);
    }
}
