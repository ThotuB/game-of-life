package event;

import org.json.JSONException;
import org.json.JSONObject;

public interface IEvent {
    JSONObject generate() throws JSONException;
}
