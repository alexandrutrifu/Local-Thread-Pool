package org.apd.threadpool.sync;

import org.apd.executor.TaskExecutor;
import org.apd.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public interface DatabaseAccessManager {
    List<Semaphore> databaseAccess = new ArrayList<>();
    List<Semaphore> readerAccess = new ArrayList<>();
    List<Semaphore> writerAccess = new ArrayList<>();
    Semaphore enter = new Semaphore(1);
    Semaphore flag = new Semaphore(0);
    ReadersWritersCounter counter = ReadersWritersCounter.getInstance();

    static void initializeSemaphores(int storageSize) {
        restartSemaphores();
        counter.restartCounters(storageSize);

        for (int index = 0; index < storageSize; index++) {
            databaseAccess.add(index, new Semaphore(1));
            readerAccess.add(index, new Semaphore(1));
            writerAccess.add(index, new Semaphore(1));
        }
    }

    static void restartSemaphores() {
        databaseAccess.clear();
        readerAccess.clear();
        writerAccess.clear();
        flag.drainPermits();
    }
}
