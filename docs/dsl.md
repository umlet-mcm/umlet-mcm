# Mapping between DSL files and the domain models

## DSL files

What we call DSL (Domain-Specific Language) files are XML-based files used as a medium for persisting models.  
These files follow a specific XML grammar formally defined in the XSD files located in `mcm-backend/mcm-core/src/main/resources/dsl`.

The grammar was designed such that these files can be displayed for comparisons and easily understood by the user.

A model is composed of the following elements:
- A single `Model` element
- Multiple `Node` elements
- Multiple `Relation` elements

Each of these elements is stored as a single file to facilitate git comparisons.

## Elements

### Model
The `Model` element serves as the root element of the DSL. It contains metadata and descriptive attributes that 
belongs to the description panel of the model.

#### Structure:
- `id`: Unique identifier of the model.
- `title`: The name or title of the model.
- `description`: A textual description of the model.
- `tags`: A collection of tag as defined in UMLetino
    - `tag`: Individual tag entry.
- `properties`: A set of key-value pairs storing user defined properties.
    - `property`: A key-value pair.
        - `key`: Name of the property.
        - `value`: Value of the property.
- `metadata`: Contains additional information about the model that is not user-facing.

### Node
The `Node` element represents an entity in the model.

#### Structure:
- `id`: Unique identifier of the node.
- `mcm_model`: Reference to the associated model.
- `title`: Name of the node.
- `description`: Textual description of the node.
- `element_type`: Type of the element in UMLetino.
- `ppr_type`: A specific classification type.
- `tags`: A collection of tag as defined in UMLetino
    - `tag`: Individual tag entry.
- `properties`: A set of key-value pairs storing user defined properties.
    - `property`: A key-value pair.
        - `key`: Name of the property.
        - `value`: Value of the property.
- `metadata`: Contains additional information about the node that is not user-facing.

### Relation
The `Relation` element defines a connection between two nodes.
The source cannot be null.

#### Structure:
- `id`: Unique identifier of the relation.
- `mcm_model`: Reference to the associated model.
- `title`: Name of the relation.
- `description`: Textual description of the relation.
- `element_type`: Type of the relation.
- `ppr_type`: A specific classification type.
- `tags`: A collection of tag as defined in UMLetino
    - `tag`: Individual tag entry.
- `properties`: A set of key-value pairs storing user defined properties.
    - `property`: A key-value pair.
        - `key`: Name of the property.
        - `value`: Value of the property.
- `source`: Defines the source node of the relation.
    - `id`: Identifier of the source node.
    - `text`: Descriptive text associated with the source.
- `target`: Defines the target node of the relation.
    - `id`: Identifier of the target node.
    - `text`: Descriptive text associated with the target.
- `metadata`: Contains additional information about the relation that is not user-facing.

### Metadata
The `Metadata` block is not meant to be shown to the user. It contains additional attributes that do not contribute directly to the model’s semantic meaning.

#### Structure:
- `original_text`: The original textual representation of the element. It is the raw text of the panel.
- `coordinates`: Spatial positioning attributes.
    - `x`, `y`: The X and Y coordinates.
    - `w`, `h`: Width and height of the element.
- `positions`: A collection of relative positions.
    - `relative_start_point`: The relative starting position.
    - `relative_mid_points`: A collection of midpoints.
        - `relative_mid_point`: Individual midpoint.
    - `relative_end_point`: The relative ending position.
- `panel_attributes`: Additional UI-related attributes.
    - `panel_attribute`: A key-value pair.
        - `key`: Name of the attribute.
        - `value`: Value of the attribute.
- `additional_attributes`: Extra metadata fields.
    - `additional_attribute`: Individual additional attribute.

## Mapper
The mappers are responsible for building domain models from DSL and vice versa. 

## Transformer
The transformer provide services to convert domain models to DSL and vice versa using mappers.