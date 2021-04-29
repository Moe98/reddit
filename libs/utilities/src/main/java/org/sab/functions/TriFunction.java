package org.sab.functions;

@FunctionalInterface
public interface TriFunction<T, U, S> {
    S apply(T a, U b);
}
