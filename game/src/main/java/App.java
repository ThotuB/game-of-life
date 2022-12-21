import game.Game;

public class App {
        public static void main(String[] args) {
                var config = new Game.Config(
                                3,
                                3,
                                6);
                Game game = new Game(config);

                game.simulate();
        }
}
