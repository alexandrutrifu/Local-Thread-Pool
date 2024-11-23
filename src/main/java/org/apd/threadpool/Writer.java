package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;

public class Writer {
    Worker parentWorker;

    public Writer() {
    }
    public Writer(Worker parentWorker) {
        this.parentWorker = parentWorker;
    }

    public EntryResult write() {
        SharedDatabase database = ThreadPool.getSharedDatabase();
        StorageTask task = parentWorker.getTask();

        return database.addData(task.index(), task.data());
    }
}
