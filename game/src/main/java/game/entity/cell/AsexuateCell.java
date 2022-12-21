package game.entity.cell;

import event.AsexuateCellReproducedEvent;
import event.SexuateCellReproducedEvent;
import game.Game;
import org.json.JSONObject;
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

        try{
            JSONObject json = AsexuateCellReproducedEvent.generate(this);
            System.out.println(json);
            game.client.sendJson(json);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }

        //game.client.send("reproduce", this.getId().toString());
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
