package at.ac.tuwien.model.change.management.core.model.attributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The value for an attribute must be of a specific type.
 * Currently, the parser supports Integers, Floats and String.
 * If used on a Listable attribute, all elements in the list must
 * be of the given type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HasType {
    Class<?> type();
}
