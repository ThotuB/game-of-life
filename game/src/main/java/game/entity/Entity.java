package game.entity;

public abstract class Entity {
    protected Integer id;
    private static int numEntities = 0;

    public Entity() {
        this.id = generateId(this);
    }

    public Integer getId() {
        return id;
    }

    public static int generateId(Entity entity) {
        return numEntities++;
    }
}
