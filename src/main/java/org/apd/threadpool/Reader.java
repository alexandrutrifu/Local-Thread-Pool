package org.apd.threadpool;

import org.apd.executor.StorageTask;
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
        SharedDatabase database = ThreadPool.getInstance().getSharedDatabase();
        StorageTask task = parentWorker.getTask();

//        System.out.println("Thread " + parentWorker.getIndex() + " read from entry number " + task.index());

        // Synchronize access
        try {
            readerAccess[task.index()].acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers[task.index()]++;

        if (counter.readers[task.index()] == 1) {
            // Prioritize reader access
            try {
                DatabaseAccessManager.databaseAccess[task.index()].acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        DatabaseAccessManager.readerAccess[task.index()].release();

        EntryResult entryResult = database.getData(task.index());

        try {
            DatabaseAccessManager.readerAccess[task.index()].acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        counter.readers[task.index()]--;

        if (counter.readers[task.index()] == 0) {
            DatabaseAccessManager.databaseAccess[task.index()].release();
        }

        DatabaseAccessManager.readerAccess[task.index()].release();

        return entryResult;
    }
}
