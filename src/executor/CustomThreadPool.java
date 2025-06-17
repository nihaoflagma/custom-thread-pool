package executor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;

    private final BlockingQueue<Runnable> taskQueue;
    private final Set<Worker> workers = ConcurrentHashMap.newKeySet();
    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

    private final AtomicInteger currentPoolSize = new AtomicInteger(0);
    private volatile boolean isShutdown = false;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, int minSpareThreads,
                            long keepAliveTime, TimeUnit timeUnit, int queueSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = minSpareThreads;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;

        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.threadFactory = new CustomThreadFactory("MyPool");
        prestartCoreThreads();
    }

    private void prestartCoreThreads() {
        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    private void addWorker() {
        if (currentPoolSize.get() >= maxPoolSize) return;
        Worker worker = new Worker(this, taskQueue, keepAliveTime, timeUnit);
        workers.add(worker);
        currentPoolSize.incrementAndGet();
        worker.start();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            rejectionHandler.rejected(command);
            return;
        }

        if (!taskQueue.offer(command)) {
            
            if (currentPoolSize.get() < maxPoolSize) {
                addWorker();
                try {
                    taskQueue.put(command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    rejectionHandler.rejected(command);
                }
            } else {
                rejectionHandler.rejected(command);
            }
        } else {
            System.out.println("[Pool] Task accepted into queue: " + command);
        }

        
        int idleThreads = currentPoolSize.get() - taskQueue.size();
        if (idleThreads < minSpareThreads && currentPoolSize.get() < maxPoolSize) {
            addWorker();
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        System.out.println("[Pool] Shutdown initiated.");
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        for (Worker worker : workers) {
            worker.interrupt();
        }
        System.out.println("[Pool] Shutdown NOW initiated.");
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public CustomThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getCurrentPoolSize() {
        return currentPoolSize.get();
    }

    public void workerTerminated(Worker worker) {
        workers.remove(worker);
        currentPoolSize.decrementAndGet();
    }
}
