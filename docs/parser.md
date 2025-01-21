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
serializing the models to XML.

## Parsing steps

1. Marshal the uxf file into the intermediary classes
2. Map the intermediary classes into the domain classes
3. Create actual relations from the relation elements

## Exporting steps

## Parsing attributes

## Handling relations

### Connections

### Bidirectional relations

Each relation is stored on the source node and the target is stored in the relation. To make sure we can
navigate these relations in both direction, bidirectional relations are replaced by two new relations one pointing
forward, the other backwards. These new relations are fully identical except for their line types which is split into
two.

When a configuration is exported the relations have to be turned back into separate elements. If a relation is bidirectional
the forward and the backward pointing relations must be merged into a single relation element.