package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class Configuration {
    @Nullable
    private String name;
    @Nullable
    private String version; // hash of the git commit
    private Set<Model> models = new HashSet<>();
}
