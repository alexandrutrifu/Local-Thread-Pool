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
        synchronized (sharedLocks.get(task.index())) {
            while (counter.writers.get(task.index()) > 0 || counter.waitingWriters.get(task.index()) > 0) {
                counter.waitingReaders.addAndGet(task.index(), 1);
                try {
                    sharedLocks.get(task.index()).wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                counter.waitingReaders.addAndGet(task.index(), -1);
            }

            counter.readers.addAndGet(task.index(), 1);
        }

        // Read action
        EntryResult entryResult = database.getData(task.index());

        synchronized (sharedLocks.get(task.index())) {
            counter.readers.addAndGet(task.index(), -1);
            if (counter.readers.get(task.index()) == 0 && counter.waitingWriters.get(task.index()) > 0) {
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

        if (counter.writers.get(task.index()) > 0 || counter.waitingWriters.get(task.index()) > 0) {
            counter.waitingReaders.addAndGet(task.index(), 1);
            enter.get(task.index()).release();
            try {
                readerAccess.get(task.index()).acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        counter.readers.addAndGet(task.index(), 1);

        if (counter.waitingReaders.get(task.index()) > 0) {
            counter.waitingReaders.addAndGet(task.index(), -1);
            readerAccess.get(task.index()).release();
        } else if (counter.waitingReaders.get(task.index()) == 0) {
            enter.get(task.index()).release();
        }

        // Read action
        EntryResult entryResult = database.getData(task.index());

        try {
            enter.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers.addAndGet(task.index(), -1);

        if (counter.readers.get(task.index()) == 0 && counter.waitingWriters.get(task.index()) > 0) {
            counter.waitingWriters.addAndGet(task.index(), -1);
            writerAccess.get(task.index()).release();
        } else if (counter.readers.get(task.index()) > 0 || counter.waitingWriters.get(task.index()) == 0) {
            enter.get(task.index()).release();
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

        counter.readers.addAndGet(task.index(), 1);

        if (counter.readers.get(task.index()) == 1) {
            // Prioritize reader access
            try {
                databaseAccess.get(task.index()).acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        readerAccess.get(task.index()).release();

        EntryResult entryResult = database.getData(task.index());

        try {
            readerAccess.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers.addAndGet(task.index(), -1);

        if (counter.readers.get(task.index()) == 0) {
            databaseAccess.get(task.index()).release();
        }

        readerAccess.get(task.index()).release();

        return entryResult;
    }
}
