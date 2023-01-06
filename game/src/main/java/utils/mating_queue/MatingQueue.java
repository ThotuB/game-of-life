package utils.mating_queue;

import utils.colors.Colors;
import utils.logger.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;

import event.factory.EventFactory;
import game.Game;
import game.entity.cell.SexuateCell;

public class MatingQueue implements Runnable {
    public final Game game;
    public final ConcurrentLinkedQueue<SexuateCell> queue = new ConcurrentLinkedQueue<>();

    public MatingQueue(Game game) {
        this.game = game;
    }

    public void add(SexuateCell cell) {
        queue.add(cell);
    }

    public void remove(SexuateCell cell) {
        queue.remove(cell);
    }

    @Override
    public void run() {
        while (true) {
            if (queue.size() < 2) {
                continue;
            }

            var cell1 = queue.poll();
            var cell2 = queue.poll();

            if (cell1 == null || cell2 == null) {
                Logger.log(Colors.Y + "One of the cells died while mating :(" + Colors.X);
                continue;
            }

            var child = new SexuateCell(game, SexuateCell.Config.random(), this, SexuateCell.State.STARVING);

            game.client.send(EventFactory.createReproduceSexuateEvent(cell1, cell2));

            Logger.log(cell1 + " and " + cell2 + Colors.C + " reproducing" + Colors.X + " -> " + child);

            cell1.hasReproduced();
            cell2.hasReproduced();

            game.spawnCell(child);
        }
    }
}
