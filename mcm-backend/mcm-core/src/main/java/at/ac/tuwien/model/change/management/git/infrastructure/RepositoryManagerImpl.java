package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class RepositoryManagerImpl implements RepositoryManager {

    private final ManagedRepositoryFactory repositoryFactory;

    public <R> R withRepository(@NonNull String repositoryName, @NonNull Function<ManagedRepository, R> function) {
        try (var repository = repositoryFactory.getRepositoryByName(repositoryName)) {
            return function.apply(repository);
        }
    }

    public void consumeRepository(@NonNull String repositoryName, @NonNull Consumer<ManagedRepository> consumer) {
        try (var repository = repositoryFactory.getRepositoryByName(repositoryName)) {
            consumer.accept(repository);
        }
    }

    public <R> R withAllRepositories(@NonNull Function<List<ManagedRepository>, R> function) {
        var repositories = repositoryFactory.getAllRepositories();
        try {
            return function.apply(repositories);
        } finally {
            repositories.forEach(ManagedRepository::close);
        }
    }

    public void consumeAllRepositories(@NonNull Consumer<List<ManagedRepository>> consumer) {
        var repositories = repositoryFactory.getAllRepositories();
        try {
            consumer.accept(repositories);
        } finally {
            repositories.forEach(ManagedRepository::close);
        }
    }
}
