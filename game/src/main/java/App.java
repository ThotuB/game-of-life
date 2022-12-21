import game.Game;

public class App {
        public static void main(String[] args) {
                var config = new Game.Config(
                                100,
                                3,
                                200);
                Game game = new Game(config);

                game.simulate();
        }
}
