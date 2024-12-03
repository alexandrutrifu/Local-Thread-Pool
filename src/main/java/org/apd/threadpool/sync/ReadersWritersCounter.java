package org.apd.threadpool.sync;

import org.apd.executor.TaskExecutor;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class ReadersWritersCounter {
    private static ReadersWritersCounter instance;
    public int capacity;
    public AtomicIntegerArray readers;
    public AtomicIntegerArray writers;
    public AtomicIntegerArray waitingReaders;
    public AtomicIntegerArray waitingWriters;

    private ReadersWritersCounter() {
        capacity = TaskExecutor.getDatabaseSize();
        restartCounters(capacity);
    }

    public void restartCounters(int storageSize) {
        capacity = storageSize;
        readers = new AtomicIntegerArray(capacity);
        writers = new AtomicIntegerArray(capacity);
        waitingReaders = new AtomicIntegerArray(capacity);
        waitingWriters = new AtomicIntegerArray(capacity);
    }

    public static ReadersWritersCounter getInstance() {
        if (instance == null) {
            instance = new ReadersWritersCounter();
        }
        return instance;
    }
}
