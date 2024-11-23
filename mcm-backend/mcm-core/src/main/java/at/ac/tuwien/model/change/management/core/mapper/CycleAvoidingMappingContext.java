package at.ac.tuwien.model.change.management.core.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;
import org.springframework.stereotype.Component;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This class is used to avoid infinite loops when mapping objects with circular references.
 */
@Component
public class CycleAvoidingMappingContext {
    // Map that stores the already mapped instances
    private final Map<Object, Object> knownInstances = new IdentityHashMap<Object, Object>();

    /**
     * Get the mapped instance of the source object.
     *
     * @param source     The source object
     * @param targetType The target type
     * @param <T>        The type of the target
     * @return The mapped instance
     */
    @BeforeMapping
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        return (T) knownInstances.get( source );
    }

    /**
     * Store the mapped instance of the source object.
     *
     * @param source The source object
     * @param target The target object
     */
    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.put( source, target );
    }
}
