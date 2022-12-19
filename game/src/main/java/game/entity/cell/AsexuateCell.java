package game.entity.cell;

import game.Game;
import utils.logger.Logger;

public class AsexuateCell extends Cell {
    public AsexuateCell(Game game, Config config) {
        super(game, config);
    }

    public AsexuateCell(Game game, Config config, State state) {
        super(game, config, state);
    }

    @Override
    public void reproduce() {
        var cell = new AsexuateCell(game, Config.random(), State.STARVING);

        Logger.log(this + " is dividing -> " + cell);
        game.spawnCell(cell);

        super.reproduce();
    }

    @Override
    public void printDetails() {
        System.out.println("AsexuateCell #" + id);
        super.printDetails();
    }
}
