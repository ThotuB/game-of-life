package event.factory;

import event.*;
import game.entity.cell.AsexuateCell;
import game.entity.cell.Cell;
import game.entity.cell.SexuateCell;

import org.json.JSONObject;

public class EventFactory {
    public static JSONObject createEvent(String type) {
        return new Event(type).generate();
    }

    public static JSONObject createCellEvent(String type, Cell cell) {
        return new CellEvent(type, cell).generate();
    }

    public static JSONObject createSpawnEvent(Cell cell) {
        return new SpawnEvent(cell).generate();
    }

    public static JSONObject createReproduceAsexuateEvent(AsexuateCell cell) {
        return new ReproduceAsexuateEvent(cell).generate();
    }

    public static JSONObject createReproduceSexuateEvent(SexuateCell cell1, SexuateCell cell2) {
        return new ReproduceSexuateEvent(cell1, cell2).generate();
    }

    public static JSONObject gameStartedEvent(int numSexuate, int numAssexuate, int numFood) {
        return new GameStartedEvent(numSexuate, numAssexuate, numFood).generate();
    }
    public static JSONObject cellDiedEvent(Cell cell, int createdFood) {
        return new CellDiedEvent(cell, createdFood).generate();
    }
}
