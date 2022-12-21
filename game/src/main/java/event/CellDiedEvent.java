package event;

import event.IEvent;
import game.entity.cell.Cell;
import game.entity.cell.SexuateCell;
import org.json.JSONObject;

public class CellDiedEvent implements IEvent {
    private final int createdFood;
    private Cell cell;

    public CellDiedEvent (Cell cell, int createdFood) {
        this.cell = cell;
        this. createdFood = createdFood;
    }

    public JSONObject generate() {
        return new JSONObject()
                .put("type", "cell-died")
                .put("cell_id", cell.getId())
                .put("cell_type", cell instanceof game.entity.cell.SexuateCell)
                .put("created_food", createdFood);
    }
}
