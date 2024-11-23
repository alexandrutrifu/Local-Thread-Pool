package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.storage.SharedDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
    private static SharedDatabase sharedDatabase;
    private int capacity;
    private static ThreadPool instance;
    private List<Thread> coreWorkers;
    private static BlockingDeque<StorageTask> tasks;
    private static AtomicInteger freeWorkers;
    private static boolean isRunning;
    private static Semaphore availableTasks;

    private ThreadPool() {
    }
    private ThreadPool(SharedDatabase database, int capacity) {
        sharedDatabase = database;
        this.capacity = capacity;
        this.coreWorkers = Collections.synchronizedList(new ArrayList<>());
        tasks = new LinkedBlockingDeque<>();
        freeWorkers = new AtomicInteger(capacity);
        availableTasks = new Semaphore(0);
        isRunning = true;

        // Start core worker-threads
        initializeWorkers();
    }

    private void initializeWorkers() {
        for (int index = 0; index < capacity; index++) {
            coreWorkers.add(new Thread(new Worker(index)));
            coreWorkers.get(index).start();
        }
    }

    public static ThreadPool getInstance() {
        return instance;
    }

    public static ThreadPool getInstance(SharedDatabase database, int capacity) {
        if (instance == null) {
            instance = new ThreadPool(database, capacity);
        }
        return instance;
    }

    public void submitTask(StorageTask task) {
        tasks.offer(task);

        // Signal available task through semaphore release()
        availableTasks.release();
    }

    public void shutdown() {
        while (!tasks.isEmpty()) {}

        // Wait for threads to finish
        for (Thread thread: coreWorkers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        isRunning = false;
    }

    public int getCapacity() {
        return capacity;
    }

    public static AtomicInteger getFreeWorkers() {
        return freeWorkers;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static Semaphore getAvailableTasks() {
        return availableTasks;
    }

    public static BlockingDeque<StorageTask> getTasks() {
        return tasks;
    }

    public List<Thread> getCoreWorkers() {
        return coreWorkers;
    }

    public static SharedDatabase getSharedDatabase() {
        return sharedDatabase;
    }
}
