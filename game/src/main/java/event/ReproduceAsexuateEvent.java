package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.AsexuateCell;
import org.json.JSONException;
import org.json.JSONObject;

public class ReproduceAsexuateEvent implements IEvent {
    private final AsexuateCell cell;

    public ReproduceAsexuateEvent(AsexuateCell cell) {
        this.cell = cell;
    }

    public JSONObject generate() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return new JSONObject()
                .put("type", "reproduce-asexuate")
                .put("cell_id", cell.getId());
    };
}
