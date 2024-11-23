package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;

public class Reader {
    Worker parentWorker;

    public Reader() {
    }
    public Reader(Worker parentWorker) {
        this.parentWorker = parentWorker;
    }

    public EntryResult read() {
        SharedDatabase database = ThreadPool.getSharedDatabase();
        StorageTask task = parentWorker.getTask();

        System.out.println("Thread " + parentWorker.getTask() + " updated entry number " + task.index());

        return database.getData(task.index());
    }
}
