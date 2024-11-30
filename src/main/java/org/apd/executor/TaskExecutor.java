package org.apd.executor;

import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;
import org.apd.threadpool.sync.DatabaseAccessManager;
import org.apd.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* DO NOT MODIFY THE METHODS SIGNATURES */
public class TaskExecutor implements DatabaseAccessManager {
    private static SharedDatabase sharedDatabase;
    public static List<EntryResult> result;

    public TaskExecutor(int storageSize, int blockSize, long readDuration, long writeDuration) {
        sharedDatabase = new SharedDatabase(storageSize, blockSize, readDuration, writeDuration);
    }

    public List<EntryResult> ExecuteWork(int numberOfThreads, List<StorageTask> tasks, LockType lockType) {
        // Instantiate thread pool and start worker threads
        ThreadPool threadPool = ThreadPool.getInstance(sharedDatabase, numberOfThreads);

        threadPool.initializeWorkers();

        // Initialize synchronization mechanisms
        DatabaseAccessManager.initializeSemaphores();

        result = Collections.synchronizedList(new ArrayList<>());

        // Assign tasks to worker threads
        for (StorageTask task: tasks) {
//            System.out.println("Pushing...");
            threadPool.submitTask(task);
        }

        // Send shutdown signal
        // Exiting
        try {
            flag.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        System.out.println("Semafor");

        threadPool.shutdown();

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

    public static int getDatabaseSize() {
        return sharedDatabase.getSize();
    }
}
