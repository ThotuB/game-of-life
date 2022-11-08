package game;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MatingQueue {
    public final Lock reproduceLock = new ReentrantLock();
    private ArrayList<SexuateCell> reproducingCells;
    public MatingQueue() {
        reproducingCells = new ArrayList<>();
    }
    public void removeCell(SexuateCell cell) {
        reproducingCells.remove(cell);
    }
    public void addCellToReproducingList(SexuateCell sexuateCell) {
        reproducingCells.add(sexuateCell);
    }
    public SexuateCell popFirstCellFromReproducingList() {
        SexuateCell cell = reproducingCells.get(0);
        reproducingCells.remove(0);
        return cell;
    }
    public boolean cellAlreadyExistsInReproducingList(SexuateCell cell) {
        return reproducingCells.contains(cell);
    }
    public int getSizeOfReproducingList() {
        return reproducingCells.size();
    }

}
