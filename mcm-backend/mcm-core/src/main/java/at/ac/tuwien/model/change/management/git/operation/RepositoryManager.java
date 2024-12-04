package at.ac.tuwien.model.change.management.git.operation;

import org.eclipse.jgit.lib.Repository;

import java.util.List;

public interface RepositoryManager {

    Repository accessRepository(String repositoryName);

    List<Repository> listRepositories();
}
