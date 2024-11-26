package at.ac.tuwien.model.change.management.core.model.attributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An attribute may have multiple values.
 * The value(s) for keys with this annotations will always be parsed to lists.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Listable {
}
