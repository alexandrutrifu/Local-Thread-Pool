# ThreadPools in Concurrent Database Access

## Thread Pool - Performance Improvements

Using a thread pool offers several advantages for application performance:
- The limited size of the thread pool allows control over the number of threads running simultaneously, thus **avoiding the overhead** caused by system overload.
- Reusing threads **eliminates the cost of creating and destroying them**, both in terms of time and required resources.

The operation of extracting a task from the queue has been synchronized using a _semaphore_, ensuring that Worker threads are notified immediately when a task becomes available. This **reduces thread wait time**, while also preventing concurrent access to the queue. Additionally, task distribution is **fair and uniform**.

_Notice:_ If the task queue is empty but submissions are not yet complete, threads will wait for a new task signal.

The context of the application (i.e., accessing and updating entries in a database) underscores the usefulness of a thread pool:
- Read and write operations may target independent database entries, allowing them to be parallelized.
    - When targeting different entries (where synchronization is not required), **blocking the main thread** is avoided, as it does not have to wait for each operation to complete.
    - When operations target the same entry, **concurrent access** is avoided through the implementation of **mutual exclusion** mechanisms.

## Notes on corresponding ExecutorTests

The tests are divided into two categories, based on the task priority type: _reader-priority_ and _writer-priority_.

Regardless of priority type, the tests vary based on the parameters associated with each scenario:
- the number of threads in the pool;
- the total number of tasks;
- the proportion of Reader/Writer tasks:
    - e.g., in reader-priority tests, Reader tasks are less frequent than Writer tasks, to verify the prioritization of the former.
- the size of database entries;
- waiting times for verifying correct synchronization:
    - used to test exclusive access to database entries.