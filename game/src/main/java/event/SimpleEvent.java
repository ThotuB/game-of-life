package event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.cell.Cell;
import org.json.JSONException;
import org.json.JSONObject;

public class SimpleEvent {
    public static JSONObject generate(Cell cell, String event) throws JSONException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return new JSONObject()
                .put("type", event)
                .put("Cell1", new JSONObject()
                        .put("id", cell.getId())
                );
    };
}
