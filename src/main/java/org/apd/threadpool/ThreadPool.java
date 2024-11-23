package org.apd.threadpool;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ThreadPool {
    private int capacity;
    private static ThreadPool instance;
    private BlockingDeque<Runnable> workerQueue;

    private ThreadPool() {
    }
    private ThreadPool(int capacity) {
        this.capacity = capacity;
        this.workerQueue = new LinkedBlockingDeque<>(capacity);
    }

    public static ThreadPool getInstance(int capacity) {
        if (instance == null) {
            instance = new ThreadPool(capacity);
        }
        return instance;
    }

    public boolean offerRunnable(Runnable worker) {
        return workerQueue.offer(worker);
    }

    public int getCapacity() {
        return capacity;
    }

    public BlockingDeque<Runnable> getWorkerQueue() {
        return workerQueue;
    }
}
