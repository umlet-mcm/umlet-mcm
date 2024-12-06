package at.ac.tuwien.model.change.management.git.operation;

import lombok.NonNull;
import org.eclipse.jgit.lib.Repository;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RepositoryManager {

    Repository accessRepository(String repositoryName);

    List<Repository> listRepositories();

    default <R> R withRepository(@NonNull String repositoryName, @NonNull Function<Repository, R> function) {
        try (Repository repository = accessRepository(repositoryName)) {
            return function.apply(repository);
        }
    }

    default void consumeRepository(@NonNull String repositoryName, @NonNull Consumer<Repository> consumer) {
        try (Repository repository = accessRepository(repositoryName)) {
            consumer.accept(repository);
        }
    }

    default <R> R withAllRepositories(@NonNull Function<List<Repository>, R> function) {
        List<Repository> repositories = listRepositories();
        try {
            return function.apply(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }

    default void consumeAllRepositories(@NonNull Consumer<List<Repository>> consumer) {
        List<Repository> repositories = listRepositories();
        try {
            consumer.accept(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }

}
