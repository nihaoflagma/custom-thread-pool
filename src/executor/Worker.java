package executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPool pool;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;

    public Worker(CustomThreadPool pool, BlockingQueue<Runnable> taskQueue, long keepAliveTime, TimeUnit unit) {
        super(pool.getThreadFactory().newThread(null));
        this.taskQueue = taskQueue;
        this.pool = pool;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = unit;
    }

    @Override
    public void run() {
        try {
            while (!pool.isShutdown()) {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    System.out.println("[Worker] " + getName() + " executes " + task);
                    task.run();
                } else if (pool.getCurrentPoolSize() > pool.getCorePoolSize()) {
                    System.out.println("[Worker] " + getName() + " idle timeout, stopping.");
                    pool.workerTerminated(this);
                    return;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[Worker] " + getName() + " interrupted.");
        } finally {
            System.out.println("[Worker] " + getName() + " terminated.");
        }
    }
}
