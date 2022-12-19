package utils.queue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import game.entity.cell.SexuateCell;

public class MatingQueue {
    private SexuateCell mate;
    private final Lock lock = new ReentrantLock();

    public MatingQueue() {
    }

    private void set(SexuateCell cell) {
        lock.lock();
        try {
            this.mate = cell;
        } finally {
            lock.unlock();
        }
    }

    private SexuateCell get() {
        lock.lock();
        try {
            SexuateCell mate = this.mate;
            this.mate = null;
            return mate;
        } finally {
            lock.unlock();
        }
    }

    public SexuateCell findPartner(SexuateCell cell) {
        if (mate == null) {
            set(cell);
            return null;
        }

        if (mate == cell) {
            return cell;
        }

        return get();
    }

    public void tryRemove(SexuateCell cell) {
        if (mate == cell) {
            get();
        }
    }
}
