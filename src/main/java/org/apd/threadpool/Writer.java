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
        return null;
    }

    private EntryResult prioritizeWriters1(StorageTask task, SharedDatabase database) {
        return null;
    }

    private static EntryResult prioritizeReaders(StorageTask task, SharedDatabase database) {
        // Synchronize
        try {
            databaseAccess.get(task.index()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Writing...");

        EntryResult entryResult = database.addData(task.index(), task.data());

        databaseAccess.get(task.index()).release();

        return entryResult;
    }
}
