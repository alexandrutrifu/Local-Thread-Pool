package org.apd.threadpool.sync;

import org.apd.executor.LockType;
import org.apd.executor.TaskExecutor;
import org.apd.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public interface DatabaseAccessManager {
    List<Semaphore> databaseAccess = new ArrayList<>();
    List<Semaphore> readerAccess = new ArrayList<>();
    List<Semaphore> writerAccess = new ArrayList<>();
    List<Semaphore> enter = new ArrayList<>();
    List<Object> sharedLocks = new ArrayList<>();
    List<Object> readerLocks = new ArrayList<>();
    List<Object> writerLocks = new ArrayList<>();
    Semaphore flag = new Semaphore(0);
    ReadersWritersCounter counter = ReadersWritersCounter.getInstance();

    static void initializeSemaphores(int storageSize) {
        restartSemaphores();
        counter.restartCounters(storageSize);

        if (TaskExecutor.getLockType() == LockType.ReaderPreferred) {
            initializeReaderPreferred(storageSize);
        } else {
            initializeWriterPreferred(storageSize);
        }
    }

    static void initializeWriterPreferred(int storageSize) {
        for (int index = 0; index < storageSize; index++) {
            sharedLocks.add(new Object());
            readerLocks.add(new Object());
            writerLocks.add(new Object());
            readerAccess.add(new Semaphore(0));
            writerAccess.add(new Semaphore(0));
            enter.add(new Semaphore(1));
        }
    }

    static void initializeReaderPreferred(int storageSize) {
        for (int index = 0; index < storageSize; index++) {
            databaseAccess.add(new Semaphore(1));
            readerAccess.add(new Semaphore(1));
            writerAccess.add(new Semaphore(1));
        }
    }

    static void restartSemaphores() {
        databaseAccess.clear();
        readerAccess.clear();
        writerAccess.clear();
        sharedLocks.clear();
        enter.clear();
        flag.drainPermits();
    }
}
