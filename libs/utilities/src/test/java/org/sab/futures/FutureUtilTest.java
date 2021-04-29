package org.sab.futures;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class FutureUtilTest {

    private static String loopUtilDone(Future<String> future) {
        while (!future.isDone()) {
            // Keep waiting
        }

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    
    @Test
    public void shouldWaitForFuture() {
        final int actionThreadCount = 1;
        final ExecutorService actionPool = Executors.newFixedThreadPool(actionThreadCount);

        final Callable<String> sayHi = () -> {
            Thread.sleep(1000);
            return "lazy hi";
        };

        final Future<String> sayHiFuture = actionPool.submit(sayHi);

        final int observerThreadCount = 2;
        final ExecutorService observerPool = Executors.newFixedThreadPool(observerThreadCount);

        final Future<String> loopToObserve = observerPool.submit(() -> loopUtilDone(sayHiFuture));

        final Future<String> awaitToObserve = observerPool.submit(() -> 
            FutureUtil.await(sayHiFuture, e -> null)
        );

        final String expected = loopUtilDone(loopToObserve);
        final String actual = loopUtilDone(awaitToObserve);

        assertEquals(expected, actual);
    }
}
