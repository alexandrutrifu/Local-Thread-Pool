package org.apd.threadpool.sync;

import org.apd.executor.TaskExecutor;
import org.apd.threadpool.ThreadPool;

import java.util.concurrent.Semaphore;

public interface DatabaseAccessManager {
    Semaphore[] databaseAccess = new Semaphore[TaskExecutor.getDatabaseSize()];
    Semaphore[] readerAccess = new Semaphore[TaskExecutor.getDatabaseSize()];
    Semaphore[] writerAccess = new Semaphore[TaskExecutor.getDatabaseSize()];
    Semaphore flag = new Semaphore(0);
    ReadersWritersCounter counter = ReadersWritersCounter.getInstance();

    static void initializeSemaphores() {
        for (int index = 0; index < databaseAccess.length; index++) {
            databaseAccess[index] = new Semaphore(1);
            readerAccess[index] = new Semaphore(1);
            writerAccess[index] = new Semaphore(1);
        }
    }
}
