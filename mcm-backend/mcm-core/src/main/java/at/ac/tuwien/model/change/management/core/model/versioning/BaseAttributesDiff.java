package at.ac.tuwien.model.change.management.core.model.versioning;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class BaseAttributesDiff {
    private final BaseAttributes element;
    private final String diffType;
    private final String content;
}
