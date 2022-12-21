package event;

import org.json.JSONObject;

public class GameStartedEvent implements IEvent{
    private final int numSexuate;
    private final int numAsexuate;
    private final int numFood;

    public GameStartedEvent(int numSexuate, int numAsexuate, int numFood) {
        this.numAsexuate = numAsexuate;
        this.numSexuate = numSexuate;
        this.numFood = numFood;
    }

    public JSONObject generate () {
        return new JSONObject()
                .put("type", "game-started")
                .put("num_sexuate", numSexuate)
                .put("num_asexuate", numAsexuate)
                .put("num_food", numFood)
                .put("total_cells", numSexuate + numAsexuate);
    }

}
