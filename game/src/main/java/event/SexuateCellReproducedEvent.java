package event;

import game.entity.cell.SexuateCell;
import org.json.JSONException;
import org.json.JSONObject;

public class SexuateCellReproducedEvent implements IEvent {
    private final SexuateCell cell;
    private final SexuateCell cell2;

    public SexuateCellReproducedEvent(SexuateCell cell1, SexuateCell cell2) {
        this.cell = cell1;
        this.cell2 = cell2;
    }

    @Override
    public JSONObject generate() throws JSONException {
        return new JSONObject()
                .put("Cell 1", new JSONObject()
                        .put("id", Integer.toString(cell.getId()))
                        .put("d", "e")
                ).put("Cell 2", new JSONObject()
                        .put("id",Integer.toString(cell2.getId()))
                        .put("x", "y"));
    };
}

