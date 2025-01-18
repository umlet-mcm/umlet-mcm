package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.NonNull;

public record DSLElement<T extends BaseAttributes>(@NonNull T element, @NonNull String dsl) {
}
