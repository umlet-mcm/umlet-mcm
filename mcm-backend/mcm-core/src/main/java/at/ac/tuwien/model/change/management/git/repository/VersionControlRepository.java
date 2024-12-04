package at.ac.tuwien.model.change.management.git.repository;

import java.util.Optional;

public interface VersionControlRepository {

    Optional<String> getCurrentVersion(String repositoryName);
}
