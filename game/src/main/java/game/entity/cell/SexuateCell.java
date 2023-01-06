package game.entity.cell;

import game.Game;
import utils.logger.Logger;
import utils.mating_queue.MatingQueue;

public class SexuateCell extends Cell {
    private final MatingQueue matingQueue;
    private boolean isTryingToMate = false;

    public SexuateCell(Game game, Config config, MatingQueue matingQueue) {
        super(game, config);
        this.matingQueue = matingQueue;
    }

    public SexuateCell(Game game, Config config, MatingQueue matingQueue, State state) {
        super(game, config, state);
        this.matingQueue = matingQueue;
    }

    @Override
    public void reproduce() {
        if (isTryingToMate) {
            return;
        }
        Logger.log(this + " is trying to mate");
        isTryingToMate = true;
        matingQueue.add(this);
    }

    public void hasReproduced() {
        isTryingToMate = false;
        super.reproduce();
    }

    @Override
    protected void die() {
        matingQueue.remove(this);
        super.die();
    }

    @Override
    public void printDetails() {
        System.out.println("SexuateCell #" + id);
        super.printDetails();
    }
}