package at.ac.tuwien.model.change.management.git.infrastructure;

import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages access to Git repositories represented by {@link ManagedRepository} objects.
 */
public interface RepositoryManager {

    /**
     * Executes the supplied function with the repository of the given name and closes the repository afterward.
     *
     * @param repositoryName the name of the repository
     * @param function       the function to execute
     * @param <R>            the return type of the function
     * @return the result of the function
     */
    <R> R withRepository(@NonNull String repositoryName, @NonNull Function<ManagedRepository, R> function);

    /**
     * Executes the supplied consumer with the repository of the given name and closes the repository afterward.
     *
     * @param repositoryName the name of the repository
     * @param consumer       the consumer function to execute
     */
    void consumeRepository(@NonNull String repositoryName, @NonNull Consumer<ManagedRepository> consumer);

    /**
     * Executes the supplied function with all repositories and closes them afterward.
     *
     * @param function the function to execute
     * @param <R>      the return type of the function
     * @return the result of the function
     */
    <R> R withAllRepositories(@NonNull Function<List<ManagedRepository>, R> function);

    /**
     * Executes the supplied consumer with all repositories and closes them afterward.
     *
     * @param consumer the consumer function to execute
     */
    void consumeAllRepositories(@NonNull Consumer<List<ManagedRepository>> consumer);
}
