package game.entity.cell;

import event.SexuateCellReproducedEvent;
import game.Game;
import org.json.JSONException;
import utils.logger.Logger;
import utils.queue.MatingQueue;

public class SexuateCell extends Cell {
    private MatingQueue matingQueue;

    public SexuateCell(Game game, Config config, MatingQueue matingQueue) {
        super(game, config);
        this.matingQueue = matingQueue;
    }

    public SexuateCell(Game game, Config config, MatingQueue matingQueue, State state) {
        super(game, config, state);
        this.matingQueue = matingQueue;
    }

    @Override
    public void reproduce()  {
        SexuateCell partner = matingQueue.findPartner(this);

        if (partner == null) {
            Logger.log(this + " waiting for a partner");
            return;
        }
        if (partner == this) {
            return;
        }

        var child = new SexuateCell(game, Config.random(), matingQueue, State.STARVING);
        SexuateCellReproducedEvent test = new SexuateCellReproducedEvent(this, partner);
        try{
            System.out.println(test.generate());

        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        game.client.send("reproduce", this.getId().toString());
        Logger.log(this + " and " + partner + " reproducing -> " + child);
        game.spawnCell(child);

        super.reproduce();
    }

    @Override
    protected void die() {
        super.die();
        matingQueue.tryRemove(this);
    }

    @Override
    public void printDetails() {
        System.out.println("SexuateCell #" + id);
        super.printDetails();
    }
}