package org.sab.rabbitmq;

public interface TriFunction<T, U, S> {
    S invoke(T a, U b);
}
