import game.Game;

import java.lang.reflect.Modifier;


public class App {
        public static void main(String[] args) {
                 var config = new Game.Config(
                 5,
                 5,
                 50);
                 Game game = new Game(config);

                 game.simulate();
        }
}
