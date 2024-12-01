package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.executor.TaskExecutor;
import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;

import org.apd.threadpool.sync.DatabaseAccessManager;

public class Reader implements DatabaseAccessManager {
    private Worker parentWorker;

    public Reader() {
    }
    public Reader(Worker parentWorker) {
        this.parentWorker = parentWorker;
    }

    public EntryResult read() {
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
        return null;
    }

    private EntryResult prioritizeWriters1(StorageTask task, SharedDatabase database) {
        try {
            enter.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (counter.writers[task.index()] > 0 || counter.waitingWriters[task.index()] > 0) {
            counter.waitingReaders[task.index()]++;
            enter.release();
            try {
                readerAccess.get(task.index()).acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        counter.readers[task.index()]++;

        if (counter.waitingReaders[task.index()] > 0) {
            counter.waitingReaders[task.index()]--;
            readerAccess.get(task.index()).release();
        } else if (counter.waitingReaders[task.index()] == 0) {
            enter.release();
        }

        System.out.println("Reader " + parentWorker.getIndex() + " is reading from entry " + task.index() + "...");

        // Read action
        EntryResult entryResult = database.getData(task.index());

        try {
            enter.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers[task.index()]--;

        if (counter.readers[task.index()] == 0 && counter.waitingWriters[task.index()] > 0) {
            counter.waitingWriters[task.index()]--;
            writerAccess.get(task.index()).release();
        } else if (counter.readers[task.index()] > 0 || counter.waitingWriters[task.index()] == 0) {
            enter.release();
        }

        return entryResult;
    }

    private EntryResult prioritizeReaders(StorageTask task, SharedDatabase database) {
        // Synchronize access
        try {
            readerAccess.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers[task.index()]++;

        if (counter.readers[task.index()] == 1) {
            // Prioritize reader access
            try {
                databaseAccess.get(task.index()).acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Reading...");

        readerAccess.get(task.index()).release();

        EntryResult entryResult = database.getData(task.index());

        try {
            readerAccess.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers[task.index()]--;

        if (counter.readers[task.index()] == 0) {
            databaseAccess.get(task.index()).release();
        }

        readerAccess.get(task.index()).release();

        return entryResult;
    }
}
