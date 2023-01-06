package event;

public record SpawnDto(
        CellDto cell) {
    public static final String type = "spawn";

    public record CellDto(
            int id,
            ConfigDto config,
            boolean type) {
        public record ConfigDto(
                int fpr,
                int time_full,
                int time_starve) {
        }
    }
}