package event;

import com.google.gson.Gson;
import game.entity.cell.SexuateCell;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

public class SexuateCellReproducedEvent
{
    public static JSONObject generate(SexuateCell cell, SexuateCell cell2) throws JSONException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();


        return new JSONObject()
                .put("type", "sexuateReproduce")
                .put("Cell1", new JSONObject()
                        .put("id", cell.getId())
                        .put("config", gson.toJson(cell.getConfig()))
                        .put("foodEaten", cell.getFoodConsumed())
                ).put("Cell2", new JSONObject()
                        .put("id",cell2.getId())
                        .put("config", gson.toJson(cell2.getConfig()))
                        .put("foodEaten", cell2.getFoodConsumed())
                );
    };
}

