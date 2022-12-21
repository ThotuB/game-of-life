package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.Cell;
import org.json.JSONException;
import org.json.JSONObject;

public class CellEvent implements IEvent {
    private final String event;
    private final Cell cell;

    public CellEvent(String event, Cell cell) {
        this.event = event;
        this.cell = cell;
    }

    public JSONObject generate() throws JSONException {
        return new JSONObject()
                .put("type", event)
                .put("Cell1", new JSONObject()
                        .put("id", cell.getId())
                );
    };
}
