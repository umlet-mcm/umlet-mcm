package at.ac.tuwien.model.change.management.core.utils;

import lombok.SneakyThrows;

import java.util.function.Function;

import static lombok.Lombok.sneakyThrow;

// workaround that allows (sort of) throwing checked exceptions from lambda expressions
// https://stackoverflow.com/questions/27644361/how-can-i-throw-checked-exceptions-from-inside-java-8-lambdas-streams
public final class LambdaExceptionUtils {
    private LambdaExceptionUtils() {
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    public static <T, R, E extends Exception> Function<T, R> rethrowFunction(ThrowingFunction<T, R, E> function) throws E {
        return input -> {
            try {
                return function.apply(input);
            } catch (Exception e) {
                throw sneakyThrow(e);
            }
        };
    }

    public static <T, E extends Exception> ThrowingConsumer<T, E> rethrowConsumer(ThrowingConsumer<T, E> consumer) throws E {
        return input -> {
            try {
                consumer.accept(input);
            } catch (Exception e) {
                throw sneakyThrow(e);
            }
        };
    }
}
