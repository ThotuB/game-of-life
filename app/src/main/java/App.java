import game.Game;

public class App {
    public static void main(String[] args) {
        Game.Config config = new Game.Config(
            2,
            3,
            200,
            2,
            20
        );

        Game game = new Game(config);

        System.out.println(game);

        game.simulate();

        System.out.println(game);
    }
}
