package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.AsexuateCell;
import org.json.JSONException;
import org.json.JSONObject;

public class AsexuateCellReproducedEvent {
    public static JSONObject generate(AsexuateCell cell) throws JSONException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return new JSONObject()
                .put("type", "asexuateReproduce")
                .put("Cell1", new JSONObject()
                        .put("id", Integer.toString(cell.getId()))
                        .put("config", gson.toJson(cell.getConfig()))
                        .put("foodEaten", Integer.toString(cell.getFoodConsumed()))
                );
    };
}
