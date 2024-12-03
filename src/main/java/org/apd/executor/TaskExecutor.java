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
    private static LockType lockType;
    public static List<EntryResult> result;
    private static int tasksToSubmit;

    public TaskExecutor(int storageSize, int blockSize, long readDuration, long writeDuration) {
        sharedDatabase = new SharedDatabase(storageSize, blockSize, readDuration, writeDuration);
    }

    public List<EntryResult> ExecuteWork(int numberOfThreads, List<StorageTask> tasks, LockType lockType) {
        // Initialize lock type and task number
        TaskExecutor.lockType = lockType;
        TaskExecutor.tasksToSubmit = tasks.size();

        // Restart synchronization mechanisms
        DatabaseAccessManager.initializeSemaphores(sharedDatabase.getSize());

        // Instantiate thread pool and start worker threads
        ThreadPool threadPool = ThreadPool.getInstance(numberOfThreads);

        threadPool.setIsRunning(true);
        threadPool.initializeWorkers();

        result = Collections.synchronizedList(new ArrayList<>());

        // Assign tasks to worker threads
        for (StorageTask task: tasks) {
            threadPool.submitTask(task);
            tasksToSubmit--;
        }

        // Send shutdown signal
        try {
            flag.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        threadPool.shutdown();

        System.out.println("Finished!");

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
    public static SharedDatabase getSharedDatabase() {
        return sharedDatabase;
    }
    public static LockType getLockType() {
        return lockType;
    }
    public static int getTasksLeft() {
        return tasksToSubmit;
    }
}
