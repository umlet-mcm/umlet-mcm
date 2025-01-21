package at.ac.tuwien.model.change.management.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode
public class Configuration {
    private String name;
    private ConfigurationVersion version; // hash of the git commit
    private Set<Model> models = new HashSet<>();

    public String getVersionHash() {
        return version != null ? version.hash() : null;
    }

    public String getVersionName() {
        return version != null ? version.name() : null;
    }

    public String getVersionCustomName() {
        return version != null ? version.customName() : null;
    }
}
