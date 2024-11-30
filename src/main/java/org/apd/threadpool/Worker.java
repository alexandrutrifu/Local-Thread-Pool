package org.apd.threadpool;

import org.apd.executor.StorageTask;
import org.apd.executor.TaskExecutor;
import org.apd.threadpool.sync.DatabaseAccessManager;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Worker implements DatabaseAccessManager, Runnable {
    private ThreadPool threadPool;
    private int index;
    private StorageTask task;
    private Semaphore busy;
    private Semaphore tasksAvailable;

    public Worker() {
    }
    public Worker(int index) {
        this(index, null);
    }
    public Worker(int index, StorageTask task) {
        this.threadPool = ThreadPool.getInstance();
        this.index = index;
        this.task = task;
        this.busy = new Semaphore(0);
        this.tasksAvailable = threadPool.getAvailableTasks();
    }

    @Override
    public void run() {
        while (threadPool.isRunning()) {
            // Tries to get a new task
            try {
                tasksAvailable.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!threadPool.isRunning()) {
                return;
            }

            // Polls from task pool
            try {
                assignTask(threadPool.getTasks().poll(500, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Handle request
            if (task.isWrite()) {
                TaskExecutor.result.add(new Writer(this).write());
            } else {
                TaskExecutor.result.add(new Reader(this).read());
            }

            if (threadPool.getTasks().isEmpty()) {
                flag.release();
                threadPool.setIsRunning(false);
            }
        }
    }

    private void assignTask(StorageTask task) {
        this.task = task;
    }

    public StorageTask getTask() {
        return task;
    }

    public int getIndex() {
        return index;
    }

    public void releaseBusy() {
        busy.release();
    }
}
