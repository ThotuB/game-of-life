package event;

import org.json.JSONObject;

public class Event implements IEvent {
    private String type;

    public Event(String type) {
        this.type = type;
    }

    public JSONObject generate() {
        return new JSONObject()
                .put("type", type);
    }
}
