package org.apd.threadpool.sync;

import org.apd.executor.TaskExecutor;

public class ReadersWritersCounter {
    private static ReadersWritersCounter instance;
    public int[] readers;
    public int[] writers;
    // TODO: waitingWriters, waitingReaders;

    private ReadersWritersCounter() {
        readers = new int[TaskExecutor.getDatabaseSize()];
        writers = new int[TaskExecutor.getDatabaseSize()];
    }

    public static ReadersWritersCounter getInstance() {
        if (instance == null) {
            instance = new ReadersWritersCounter();
        }
        return instance;
    }
}
