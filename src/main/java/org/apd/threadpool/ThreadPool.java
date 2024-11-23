package org.apd.threadpool;

import org.apd.executor.StorageTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ThreadPool {
    private int capacity;
    private static ThreadPool instance;
    private List<Runnable> coreWorkers;
    private BlockingDeque<StorageTask> waitingTasks;

    private ThreadPool() {
    }
    private ThreadPool(int capacity) {
        this.capacity = capacity;
        this.coreWorkers = Collections.synchronizedList(new ArrayList<>());
        this.waitingTasks = new LinkedBlockingDeque<>();

        // Initialize core worker-threads
        initializeWorkers();
    }

    private void initializeWorkers() {
        for (int index = 0; index < capacity; index++) {
            coreWorkers.set(index, new Worker(index));
        }
    }

    public static ThreadPool getInstance(int capacity) {
        if (instance == null) {
            instance = new ThreadPool(capacity);
        }
        return instance;
    }

    public boolean submitTask(StorageTask task) {
        // Check if core workers are busy
        for (Runnable thread: coreWorkers) {
            Worker workerThread = (Worker) thread;

            if (!workerThread.isBusy()) {
                // TODO: assign task (implement Worker method)
                return true;
            }
        }

        // If all core workers are busy, insert task into waiting queue
        return waitingTasks.offer(task);
    }

    public int getCapacity() {
        return capacity;
    }
}
