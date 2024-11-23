package org.apd.executor;

import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;
import org.apd.threadpool.ThreadPool;
import org.apd.threadpool.Worker;

import java.util.ArrayList;
import java.util.List;

/* DO NOT MODIFY THE METHODS SIGNATURES */
public class TaskExecutor {
    private final SharedDatabase sharedDatabase;

    public TaskExecutor(int storageSize, int blockSize, long readDuration, long writeDuration) {
        sharedDatabase = new SharedDatabase(storageSize, blockSize, readDuration, writeDuration);
    }

    public List<EntryResult> ExecuteWork(int numberOfThreads, List<StorageTask> tasks, LockType lockType) {
        // Instantiate thread pool and start worker threads
        ThreadPool threadPool = ThreadPool.getInstance(numberOfThreads);

        // TODO: Initialize list of resulting entries
        List<EntryResult> result = null;

        // Assign tasks to worker threads
        for (StorageTask task: tasks) {
            // TODO: save result if it's a write request
            threadPool.submitTask(task);
        }

        return result;
    }

    public List<EntryResult> ExecuteWorkSerial(List<StorageTask> tasks) {
        var results = tasks.stream().map(task -> {
            try {
                if (task.isWrite()) {
                    return sharedDatabase.addData(task.index(), task.data());
                } else {
                    return sharedDatabase.getData(task.index());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return results.stream().toList();
    }
}
