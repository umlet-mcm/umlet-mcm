package at.ac.tuwien.model.change.management.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode
public class Configuration {
    private String name;
    @Nullable
    private String version; // hash of the git commit
    private Set<Model> models = new HashSet<>();
}
