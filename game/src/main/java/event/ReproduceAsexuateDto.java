package event;

public record ReproduceAsexuateDto(
        int cell_id) {
    public static final String type = "reproduce-asexuate";
}