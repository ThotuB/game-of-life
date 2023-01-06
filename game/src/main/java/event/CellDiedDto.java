package event;

public record CellDiedDto(
        int cell_id,
        boolean cell_type,
        int created_food) {
    public static final String type = "cell-died";
}