package org.apd.threadpool.sync;

import org.apd.executor.TaskExecutor;

public class ReadersWritersCounter {
    private static ReadersWritersCounter instance;
    public int capacity;
    public int[] readers;
    public int[] writers;
    public int[] waitingReaders;
    public int[] waitingWriters;

    private ReadersWritersCounter() {
        capacity = TaskExecutor.getDatabaseSize();
        restartCounters(capacity);
    }

    public void restartCounters(int storageSize) {
        capacity = storageSize;
        readers = new int[capacity];
        writers = new int[capacity];
        waitingReaders = new int[capacity];
        waitingWriters = new int[capacity];
    }

    public static ReadersWritersCounter getInstance() {
        if (instance == null) {
            instance = new ReadersWritersCounter();
        }
        return instance;
    }
}
