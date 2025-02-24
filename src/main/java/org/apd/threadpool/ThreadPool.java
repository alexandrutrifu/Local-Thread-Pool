package org.apd.threadpool;

import org.apd.executor.LockType;
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
    private int capacity;
    private static ThreadPool instance;
    private List<Thread> coreWorkers;
    private BlockingDeque<StorageTask> tasks;
    private boolean isRunning;
    private Semaphore availableTasks;

    private ThreadPool() {
    }
    private ThreadPool(int capacity) {
        this.capacity = capacity;
        coreWorkers = Collections.synchronizedList(new ArrayList<>());
        tasks = new LinkedBlockingDeque<>();
        availableTasks = new Semaphore(0, true);
        isRunning = true;
    }

    public void initializeWorkers() {
        // Reset semaphore
        availableTasks.drainPermits();

        for (int index = 0; index < capacity; index++) {
            coreWorkers.add(new Thread(new Worker(index)));
            coreWorkers.get(index).start();
        }
    }

    public static ThreadPool getInstance() {
        return instance;
    }
    public static ThreadPool getInstance(int capacity) {
        if (instance == null) {
            instance = new ThreadPool(capacity);
        }

        if (instance.coreWorkers.size() != capacity) {
            instance.capacity = capacity;
        }

        return instance;
    }

    public void submitTask(StorageTask task) {
        tasks.offer(task);

        // Signal available task through semaphore release()
        availableTasks.release();
    }

    public void shutdown() {
        isRunning = false;

        // Wake up waiting threads
        for (Thread thread: coreWorkers) {
            availableTasks.release();
        }

        // Wait for threads to finish
        for (Thread thread: coreWorkers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        coreWorkers.clear();
    }

    public boolean isRunning() {
        return isRunning;
    }
    public Semaphore getAvailableTasks() {
        return availableTasks;
    }
    public BlockingDeque<StorageTask> getTasks() {
        return tasks;
    }
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

}
