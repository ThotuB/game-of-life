package game.entity.cell;

import event.SexuateCellReproducedEvent;
import game.Game;
import org.json.JSONException;
import org.json.JSONObject;
import utils.logger.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SexuateCell extends Cell {
    private final ConcurrentLinkedQueue<SexuateCell> matingQueue;

    public SexuateCell(Game game, Config config, ConcurrentLinkedQueue<SexuateCell> matingQueue) {
        super(game, config);
        this.matingQueue = matingQueue;
    }

    public SexuateCell(Game game, Config config, ConcurrentLinkedQueue<SexuateCell> matingQueue, State state) {
        super(game, config, state);
        this.matingQueue = matingQueue;
    }

    @Override
    public void reproduce()  {
        if (matingQueue.stream().findAny().isEmpty()) {
            matingQueue.add(this);
            Logger.log(this + " waiting for a partner");
            return;
        }
        SexuateCell partner = matingQueue.stream().findAny().get();

        if (partner == this) {
            return;
        }
        matingQueue.remove(this);

        var child = new SexuateCell(game, Config.random(), matingQueue, State.STARVING);
        try{
            JSONObject json = SexuateCellReproducedEvent.generate(this, partner);
            System.out.println(json);
            game.client.sendJson(json);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }

        //game.client.send("reproduce", this.getId().toString());
        Logger.log(this + " and " + partner + " reproducing -> " + child);
        game.spawnCell(child);

        super.reproduce();
    }

    @Override
    protected void die() {
        super.die();
        matingQueue.remove(this);
    }

    @Override
    public void printDetails() {
        System.out.println("SexuateCell #" + id);
        super.printDetails();
    }
}