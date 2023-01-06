package event.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import event.*;
import game.entity.cell.AsexuateCell;
import game.entity.cell.Cell;
import game.entity.cell.SexuateCell;

import java.lang.reflect.Modifier;

public class EventFactory {
    private static final GsonBuilder builder = new GsonBuilder();
    private static final Gson gson = builder.excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

    public static String createEvent(String type) {
        return gson.toJson(new EventDto(type));
    }

    public static String createCellEvent(String type, Cell cell) {
        return gson.toJson(new CellDto(type, cell.getId()));
    }

    public static String createSpawnEvent(Cell cell) {
        return gson.toJson(new SpawnDto(
                new SpawnDto.CellDto(
                        cell.getId(),
                        new SpawnDto.CellDto.ConfigDto(
                                cell.getConfig().foodPerReproduce(),
                                cell.getConfig().timeFull(),
                                cell.getConfig().timeStarve()),
                        cell instanceof SexuateCell)));
    }

    public static String createReproduceAsexuateEvent(AsexuateCell cell) {
        return gson.toJson(new ReproduceAsexuateDto(cell.getId()));
    }

    public static String createReproduceSexuateEvent(SexuateCell cell1, SexuateCell cell2) {
        return gson.toJson(new ReproduceSexuateDto(cell1.getId(), cell2.getId()));
    }

    public static String gameStartedEvent(int numSexuate, int numAssexuate, int numFood) {
        return gson.toJson(new GameStartedDto(numSexuate, numAssexuate, numFood));
    }

    public static String cellDiedEvent(Cell cell, int createdFood) {
        return gson.toJson(new CellDiedDto(
                cell.getId(),
                cell instanceof SexuateCell,
                createdFood));
    }
}
