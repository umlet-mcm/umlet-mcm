package at.ac.tuwien.model.change.management.git.infrastructure;

import java.nio.file.Path;

public record ManagedRepositoryFile(Path path, String content)
{
}