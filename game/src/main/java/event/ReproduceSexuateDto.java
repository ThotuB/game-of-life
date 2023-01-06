package event;

public record ReproduceSexuateDto(
        int cell_1_id,
        int cell_2_id) {
    public static final String type = "reproduce-sexuate";
}