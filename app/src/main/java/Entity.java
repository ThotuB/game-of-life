public abstract class Entity {
    public int id;
    private static Map<Integer, Entity> entities = new HashMap<Integer, Entity>();

    public Entity() {
        this.id = generateId();
    }

    public static int generateId() {
        return (int) (Math.random() * 100000);
    }
}
