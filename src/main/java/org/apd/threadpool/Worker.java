package org.apd.threadpool;

import org.apd.executor.StorageTask;

import java.lang.ref.WeakReference;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker implements Runnable {
    private int index;
    private StorageTask task;
    private AtomicBoolean busy; // TODO: See if atomicity is needed
    private Semaphore tasksAvailable;

    public Worker() {
    }
    public Worker(int index) {
        this(index, null);
    }
    public Worker(int index, StorageTask task) {
        this.index = index;
        this.task = task;
        this.busy = new AtomicBoolean(false);
        this.tasksAvailable = ThreadPool.getAvailableTasks();
    }

    @Override
    public void run() {
        while (ThreadPool.isRunning()) {
            // Tries to get a new task
            try {
                tasksAvailable.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Polls from task pool
            assignTask(ThreadPool.getTasks().poll());

            // Thread is now busy
            ThreadPool.getFreeWorkers().decrementAndGet();

            // Handle request - TODO: return result to thread pool
            if (task.isWrite()) {
                new Writer(this).write();
            } else {
                new Reader(this).read();
            }

            // Set busy state to false before quitting
            busy.set(false);

            // Signal a new free worker
            ThreadPool.getFreeWorkers().incrementAndGet();
        }
    }

    private void assignTask(StorageTask task) {
        this.task = task;
        busy.set(true);
    }

    public boolean isBusy() {
        return busy.get();
    }

    public StorageTask getTask() {
        return task;
    }

    public int getIndex() {
        return index;
    }
}
