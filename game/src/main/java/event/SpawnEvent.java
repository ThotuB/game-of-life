package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.Cell;
import org.json.JSONException;
import org.json.JSONObject;

public class SpawnEvent implements IEvent {
    private final Cell cell;

    public SpawnEvent(Cell cell) {
        this.cell = cell;
    }

    public JSONObject generate() {
        return new JSONObject()
                .put("type", "spawn")
                .put("cell", new JSONObject()
                        .put("id", cell.getId())
                        .put("config", new JSONObject()
                                .put("fpr", cell.getConfig().foodPerReproduce)
                                .put("time_full", cell.getConfig().timeFull)
                                .put("time_starve", cell.getConfig().timeStarve))
                        .put("type", cell instanceof game.entity.cell.SexuateCell));
    };
}
