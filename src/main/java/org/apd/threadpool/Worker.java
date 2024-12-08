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
        this.tasksAvailable = threadPool.getAvailableTasks();
    }

    @Override
    public void run() {
        while (threadPool.isRunning() || !threadPool.getTasks().isEmpty()) {
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
            assignTask(threadPool.getTasks().poll());

            // Handle request
            if (task.isWrite()) {
                TaskExecutor.result.add(new Writer(this).write());
            } else {
                TaskExecutor.result.add(new Reader(this).read());
            }

            if (threadPool.getTasks().isEmpty() && TaskExecutor.getTasksLeft() == 0) {
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
}
