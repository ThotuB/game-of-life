package event;

public record GameStartedDto(
        int num_sexuate,
        int num_asexuate,
        int num_food
) {
    public static final String type = "game-started";
}