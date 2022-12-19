package game.entity.food;

import game.entity.Entity;

public class Food extends Entity {
    public Food() {
        super();
    }

    @Override
    public String toString() {
        return "Food #" + id;
    }
}
