package at.ac.tuwien.model.change.management.core.model.attributes;

/**
 * Reserved attributes can be defined here. When the attributes are parsed
 * the conditions specified by the annotations get checked. Keys must be
 * defined public and static.
 */
public class McmKeyDefinitions {
    @Required
    @HasType(type = String.class)
    public static String ID = "id";

    @HasType(type = String.class)
    public static String MODEL = "model";

    @Listable
    @HasType(type = String.class)
    public static String TAGS = "tags";

    public static String PPR_TYPE = "pprType";
}
