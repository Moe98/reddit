package org.sab.futures;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class FutureUtil {
    private FutureUtil() {
    }

    public static <T> T await(Future<T> future, Function<Exception, T> onFailure) {
        T result = null;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            return onFailure.apply(e);
        }
        return result;
    }
}
