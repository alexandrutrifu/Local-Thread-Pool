package org.apd.threadpool;

import org.apd.executor.StorageTask;

public class Worker implements Runnable {
    private int index;
    private StorageTask task;
    private boolean busy;

    public Worker() {
    }
    public Worker(int index) {
        this(index, null);
    }
    public Worker(int index, StorageTask task) {
        this.index = index;
        this.task = task;
        this.busy = false;
    }

    @Override
    public void run() {

    }

    public boolean isBusy() {
        return busy;
    }
}
