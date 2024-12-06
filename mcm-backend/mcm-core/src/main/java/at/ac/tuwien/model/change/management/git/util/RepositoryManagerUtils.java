package at.ac.tuwien.model.change.management.git.util;

import org.eclipse.jgit.lib.Repository;

import java.util.function.Consumer;
import java.util.function.Function;

// handles closing the JGit repository for the user after the function has been executed
public final class RepositoryManagerUtils {
    private RepositoryManagerUtils() {}

    public static <R> R withRepository(Repository repository, Function<Repository, R> function) {
        try (repository) {
            return function.apply(repository);
        }
    }

    public static <T extends Iterable<Repository>, R> R withRepositories(T repositories, Function<T, R> function) {
        try {
            return function.apply(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }

    public static void consumeRepository(Repository repository, Consumer<Repository> consumer) {
        try (repository) {
            consumer.accept(repository);
        }
    }

    public static <T extends Iterable<Repository>> void consumeRepositories(T repositories, Consumer<T> consumer) {
        try {
            consumer.accept(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }
}
