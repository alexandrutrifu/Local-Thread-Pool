package org.apd.threadpool;

import org.apd.executor.StorageTask;
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
        SharedDatabase database = ThreadPool.getInstance().getSharedDatabase();
        StorageTask task = parentWorker.getTask();

//        System.out.println("Thread " + parentWorker.getIndex() + " updated entry number " + task.index());

        // Synchronize
        try {
            databaseAccess[task.index()].acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        EntryResult entryResult = database.addData(task.index(), task.data());

        databaseAccess[task.index()].release();

        return entryResult;
    }
}
