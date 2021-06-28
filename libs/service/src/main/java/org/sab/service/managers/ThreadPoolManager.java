package org.sab.service.managers;

import org.sab.service.ServiceConstants;

import java.util.concurrent.*;

public class ThreadPoolManager {
    private ExecutorService threadPool = null;

    public void initThreadPool(int threadCount) {
        threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public void releaseThreadPool() {
        if (threadPool == null) {
            return;
        }

        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(ServiceConstants.MAX_THREAD_TIMEOUT, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    public void dispose() {
        releaseThreadPool();
        threadPool = null;
    }

    public Future<String> submit(Callable<String> callable) {
        return threadPool.submit(callable);
    }
}
