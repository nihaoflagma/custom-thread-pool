package demo;

import executor.CustomThreadPool;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool pool = new CustomThreadPool(
                2, // corePoolSize
                4, // maxPoolSize
                1, // minSpareThreads
                5, // keepAliveTime
                TimeUnit.SECONDS,
                5 // queueSize
        );

        
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            pool.execute(() -> {
                System.out.println("[Task] Start task " + id + " in " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                System.out.println("[Task] Finish task " + id + " in " + Thread.currentThread().getName());
            });
        }

        
        Thread.sleep(15000);
        pool.shutdown();
    }
}
