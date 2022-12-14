package game.entity.cell;

import event.factory.EventFactory;
import game.Game;
import utils.colors.Colors;
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

        game.client.send(EventFactory.createReproduceAsexuateEvent(this));

        Logger.log(this + " is " + Colors.C + "dividing" + Colors.X + " -> " + cell);
        game.spawnCell(cell);

        super.reproduce();
    }

    @Override
    public void printDetails() {
        System.out.println("AsexuateCell #" + id);
        super.printDetails();
    }
}
