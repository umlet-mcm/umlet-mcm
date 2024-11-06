package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class Configuration {
    private String name;
    private Set<Model> models;
}
