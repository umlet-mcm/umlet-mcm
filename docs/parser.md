# Mapping between uxf files and the domain models

## Uxf files

Models exported from Umlet are stored in uxf files.
Structurally these are XML files with the enclosing `<xml>...</xml>` tags replaced by custom ones.
These files can be uploaded to the MCM backend where they will be parsed into their Java representation.
When models or configurations are exported from the backend, new uxf files are generated from the domain models.

### Elements

Elements that build up a model are stored as `<element>` XML elements.

They contain the following inner elements:

- `<id>` The type of the element such as UMLClass, Relation etc. Not an actual identifier.
- `<coordinates>` The position of the element. The coordinates of the top left corner,
  the width and the height are stored. These values are scaled based on the `<zoom_level>` which is defined for every uxf
  file.
- `<panel_attributes>` Stores the text displayed inside the elements as well as other Umlet and custom defined attributes.
  This field is fully editable by the user when an element is selected in Umlet. Attributes for changing the visual appearance
  of the element can be set here such as `bg=...` (background color), `lt=...` (line type) etc. Lines starting with `//`
  are treated as comments. Attributes relevant to model change management are defined here, inside comments.
  E.g. `// id: "4f7a062f-f8a4-4792-9bf1-85b49b8eba70"`.
- `<additional_attributes>` Only relevant for elements of type (`<id>Relation<id>`) `Relation`. The points that define the line of the relation
  are stored here. For other elements this tag is always empty.

### Zoom level

When zooming in and out on a diagram in Umlet, the coordinates, widths and heights are scaled accordingly. The updated
coordinates are used when a model is exported. The zoom level is an integer ranging from 1 to 20 (10% to 200%). The default
zoom level is 10. In a uxf file the value is stored inside `<zoom_level>` directly under `<diagram>`. The scaling only
affects the values inside `<coordinates>` tags, other coordinates such as the ones stored in `<additional_attributes>`
for relations are unchanged.

When a model is loaded into the MCM program the coordinates are normalized to the default zoom level, 10. When a model
is exported the coordinates are scaled back according to the original zoom level. When creating a configuration from
multiple models with different zoom levels, the smallest value is selected as the overall zoom level for the configuration.

### Relations

Relations are treated differently than other elements. In the uxf file they are individual elements but in the Java
representation they are turned into actual relations. They are stored in the source elements are they store their target
elements. For bidirectional relations two separate relations have to be created.

## Domain models

### Configuration

Represents a model configuration (project) in the MCM program. It can contain multiple models. When configurations
are exported the individual models are merged into a single uxf file. It has a name which is unique identifier and
can be set by the user. By default, it is set to a random UUID.

### Model

Represents a single model in the MCM program. It contains a list of nodes and relations. It has a UUID which is unique
per configuration and a user defined name.

### Node

Any element that is not a relation. It has a UUID which is unique per configuration.

## Intermediary classes

In order to keep the domain model classes clean from JAXB annotations the uxf files are first parsed into
intermediary classes. The name of these classes always end in `...Uxf`. These classes should only be used
for storing the content of the xml elements before they are mapped into the actual model classes and for
serializing elements to XML.

The main difference between the intermediary classes and the domain classes is the way MCM attributes are stored.
In the intermediary classes all custom attributes in a single map. To make working with the domain models easier the
reserved attributes are stored in separate fields.

## Parsing steps

1. Marshal the uxf file into the intermediary classes
2. Map the intermediary classes into the domain classes
3. Create actual relations from the relation elements

From a uxf viewpoint configurations and models have the same XML structure. When a uxf is loaded into the MCM
we have to differentiate between the two. Models should always be translated into a single model whereas configurations
have to split it up into the original models. Configurations can be identified using the `configurationId` custom
attribute they contain in the `<help_text>` element. This element also contains the custom attributes coming from each
model in custom attributes that have the format `// __{modelId}_{attributeName}: value`. During parsing these attributes
are added to the corresponding models.

## Exporting steps

1. Map the domain models into intermediary classes
2. Merge previously split up bidirectional relations into single relations
3. Merge attributes of all models into a single description if we are exporting a whole configuration
4. Arrange models so they don't overlap (only for configuration export)
5. Marshall the intermediary classes into a uxf file

### Arranging models

To make sure multiple models do not overlap when models are merged into a single uxf file, a repositioning algorithm is
used. All models that don't overlap at all are left in place. For these models are combined bounding box is calculated.
After all the overlapping models are laid out from left to right next to the combined bounding box. The models are
equally spaced out and vertically centered on the center line of the combined bounding box.

The algorithm is used whenever a configuration is exported and can be used to arrange ModelDSLs by calling the 
`POST <base_url>/api/v1/model/alignModels` endpoint. This is used to arrange models when two models are merged using the
UI.

## Parsing attributes

Custom attributes can be defined in comments inside the `<panel_text>` element. This feature is used by the MCM to add
metadata to elements for versioning and can also be used by the user to create custom attributes.

The definition of attributes follow strict conventions. Attributes that are not explicitly declared as strings will be
automatically parsed into the appropriate type. The application attempts to parse them into the following types 
in order: `int`, `float`, and `string` if the previous attempts fail. Lists are handled in the same way. Currently
there's one predefined list attribute: `tags`. Tags are always stored in a list, even if there's only one tag. If the
parsing on an attribute fails, it will be removed from the element.

[Usage of custom attributes](https://colab.tuwien.ac.at/display/SE/How+to+create+properties)

## Handling relations

### Connections

Uxf files no information about which elements are connected by a relation. When a model is loaded into the MCM relations
have to be turned into actual relations that are stored in the source node and point to the target. This is done based
on the position data. We consider a relation and a node connected if one endpoint of a relation lies inside the bounding
box of the node. Umlet uses the outline of the nodes to detect connections. We do not have this information and this
might cause inconsistencies when loading a model into the MCM. There is also some tolerance for comparing positions which
might lead to incorrect connections if two connected nodes are too close apart.

### Bidirectional relations

Each relation is stored on the source node and the target is stored in the relation. To make sure we can
navigate these relations in both direction, bidirectional relations are replaced by two new relations one pointing
forward, the other backwards. These new relations are fully identical except for their line types which is split into
two and their IDs.

Relations in Umlet can be split up into 3 parts. The left end cap, the line section and the right end cap. We consider
relations to be bidirectional if it has both end caps. When splitting up a relation into two relations the line type is
also split up using regular expression. One relation gets the corresponding end cap and the line section, the other one
the line section and the other end cap.

Currently, relations that have no source node or have no target node are discarded when the model is loaded into the MCM.

When a configuration is exported the relations have to be turned back into separate elements. If a relation is bidirectional
the forward and the backward pointing relations must be merged into a single relation element. In this case the two IDs
also have to stored in the created merged relation element.