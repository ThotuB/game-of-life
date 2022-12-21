package event;

import com.google.gson.Gson;
import game.entity.cell.SexuateCell;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

public class ReproduceSexuateEvent implements IEvent {
    private final SexuateCell cell1;
    private final SexuateCell cell2;

    public ReproduceSexuateEvent(SexuateCell cell1, SexuateCell cell2) {
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    public JSONObject generate() {
        return new JSONObject()
                .put("type", "reproduce-sexuate")
                .put("cell_1_id", cell1.getId())
                .put("cell_2_id", cell2.getId());
    };
}
