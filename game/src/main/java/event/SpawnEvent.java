package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.Cell;
import org.json.JSONException;
import org.json.JSONObject;

public class SpawnEvent {
    public static JSONObject generate(Cell cell) throws JSONException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return new JSONObject()
                .put("type", "spawn")
                .put("Cell1", new JSONObject()
                        .put("id", cell.getId())
                        .put("config", gson.toJson(cell.getConfig()))
                );
    };
}
