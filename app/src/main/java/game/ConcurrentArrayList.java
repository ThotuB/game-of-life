package game;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentArrayList<T> {
    private ArrayList<T> list;
    private final Lock lock = new ReentrantLock();

    public ConcurrentArrayList() {
        this.list = new ArrayList<T>();
    }

    public ConcurrentArrayList(int initialCapacity) {
        this.list = new ArrayList<T>(initialCapacity);
    }

    public void add(T item) {
        lock.lock();
        try {
            list.add(item);
        } finally {
            lock.unlock();
        }
    }

    public void remove(T item) {
        lock.lock();
        try {
            list.remove(item);
        } finally {
            lock.unlock();
        }
    }
}
