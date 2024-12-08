package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.executor.TaskExecutor;
import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;
import org.apd.threadpool.sync.DatabaseAccessManager;

public class Writer implements DatabaseAccessManager {
    private Worker parentWorker;

    public Writer() {
    }
    public Writer(Worker parentWorker) {
        this.parentWorker = parentWorker;
    }

    public EntryResult write() {
        SharedDatabase database = TaskExecutor.getSharedDatabase();
        StorageTask task = parentWorker.getTask();

        // Check problem priorities
        switch (TaskExecutor.getLockType()) {
            case ReaderPreferred -> {
                return prioritizeReaders(task, database);
            }
            case WriterPreferred1 -> {
                return prioritizeWriters1(task, database);
            }
            case WriterPreferred2 -> {
                return prioritizeWriters2(task, database);
            }
        }

        return null;
    }

    private EntryResult prioritizeWriters2(StorageTask task, SharedDatabase database) {
        synchronized (sharedLocks.get(task.index())) {
            while (counter.writers.get(task.index()) > 0 || counter.readers.get(task.index()) > 0) {
                counter.waitingWriters.addAndGet(task.index(), 1);
                try {
                    sharedLocks.get(task.index()).wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                counter.waitingWriters.addAndGet(task.index(), -1);
            }

            counter.writers.addAndGet(task.index(), 1);
        }

        // Write action
        EntryResult entryResult = database.addData(task.index(), task.data());

        synchronized (sharedLocks.get(task.index())) {
            counter.writers.addAndGet(task.index(), -1);
            if (counter.waitingWriters.get(task.index()) == 0 && counter.waitingReaders.get(task.index()) > 0) {
                sharedLocks.get(task.index()).notifyAll();
            } else if (counter.waitingWriters.get(task.index()) > 0) {
                sharedLocks.get(task.index()).notifyAll();
            }
        }

        return entryResult;
    }

    private EntryResult prioritizeWriters1(StorageTask task, SharedDatabase database) {
        try {
            enter.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (counter.readers.get(task.index()) > 0 || counter.writers.get(task.index()) > 0) {
            counter.waitingWriters.addAndGet(task.index(), 1);
            enter.get(task.index()).release();
            try {
                writerAccess.get(task.index()).acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        counter.writers.addAndGet(task.index(), 1);
        enter.get(task.index()).release();

        // Write action
        EntryResult entryResult = database.addData(task.index(), task.data());

        try {
            enter.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.writers.addAndGet(task.index(), -1);

        if (counter.waitingWriters.get(task.index()) == 0 && counter.waitingReaders.get(task.index()) > 0) {
            counter.waitingReaders.addAndGet(task.index(), -1);
            readerAccess.get(task.index()).release();
        } else if (counter.waitingWriters.get(task.index()) > 0) {
            counter.waitingWriters.addAndGet(task.index(), -1);
            writerAccess.get(task.index()).release();
        } else if (counter.waitingReaders.get(task.index()) == 0 && counter.waitingWriters.get(task.index()) == 0) {
            enter.get(task.index()).release();
        }

        return entryResult;
    }

    private static EntryResult prioritizeReaders(StorageTask task, SharedDatabase database) {
        // Synchronize
        try {
            databaseAccess.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        EntryResult entryResult = database.addData(task.index(), task.data());

        databaseAccess.get(task.index()).release();

        return entryResult;
    }
}
