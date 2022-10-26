public abstract class Entity {
    public int id;

    public Entity() {
        this.id = generateId();
    }

    public static int generateId() {
        return (int) (Math.random() * 100000);
    }
}
