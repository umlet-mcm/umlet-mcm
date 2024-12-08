package at.ac.tuwien.model.change.management.git.operation;

import lombok.NonNull;
import org.eclipse.jgit.lib.Repository;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages access to Git repositories represented by JGit {@link Repository} objects.
 */
public interface RepositoryManager {

    /**
     * Creates a JGit {@link Repository} object for the given repository name.
     * It's possible that this repository does not exist yet, in which case it has to be initialized
     * via JGit's {@link org.eclipse.jgit.api.InitCommand}, before saving any files to it.
     * Note that the repository must be closed after usage.
     *
     * @param repositoryName the name of the repository
     * @return a JGit {@link Repository} object
     */
    Repository accessRepository(String repositoryName);

    /**
     * Lists all repositories in the path specified in GitProperties
     * {@link at.ac.tuwien.model.change.management.git.config.GitProperties}
     * Note that the repositories must be closed after usage.
     *
     * @return a list of all repositories
     */
    List<Repository> listRepositories();

    /**
     * Executes the given function with the repository of the given name and closes the repository afterward.
     * @param repositoryName the name of the repository
     * @param function the function to execute
     * @return the result of the function
     * @param <R> the return type of the function
     */
    default <R> R withRepository(@NonNull String repositoryName, @NonNull Function<Repository, R> function) {
        try (Repository repository = accessRepository(repositoryName)) {
            return function.apply(repository);
        }
    }

    /**
     * Executes the given consumer with the repository of the given name and closes the repository afterward.
     * @param repositoryName the name of the repository
     * @param consumer the consumer function to execute
     */
    default void consumeRepository(@NonNull String repositoryName, @NonNull Consumer<Repository> consumer) {
        try (Repository repository = accessRepository(repositoryName)) {
            consumer.accept(repository);
        }
    }

    /**
     * Executes the given function with all repositories and closes them afterward.
     * @param function the function to execute
     * @return the result of the function
     * @param <R> the return type of the function
     */
    default <R> R withAllRepositories(@NonNull Function<List<Repository>, R> function) {
        List<Repository> repositories = listRepositories();
        try {
            return function.apply(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }

    /**
     * Executes the given consumer with all repositories and closes them afterward.
     * @param consumer the consumer function to execute
     */
    default void consumeAllRepositories(@NonNull Consumer<List<Repository>> consumer) {
        List<Repository> repositories = listRepositories();
        try {
            consumer.accept(repositories);
        } finally {
            repositories.forEach(Repository::close);
        }
    }

}
