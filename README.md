# UMLet Model Change Management - 2024WS-ASE-PR
UMLet MCM ("Model Change Management") allows importing diagrams created with the tool [UMLet](https://github.com/umlet/umlet) into a graph database (Neo4J) and to query them via Cypher Query Language. Changes are also automatically versioned in an (internal) git repo.

This project was created for a university course at the TU Wien and was shown at the final demo, but otherwise **not been used in production** (as of February 2025).

### Versioning software for models created in UMLet(ino)

## Features

- Keep track of model versions
- Create configurations from multiple models
- Compare different versions of configurations
- Merge multiple models into a single model
- Export configurations, models and subgraphs as UXF and CSV files
- Run Neo4j queries on the models

## [Building and Running the Application](docs/usage.md)
This section provides detailed instructions on setting up and running the application.

## Docs
This section contains documentation related to the implementation of specific features. It is divided into logical sections for easy navigation. Each section leads to a separate documentation file.

### Attribute Conventions

The custom attributes stored in the uxf files follow strict conventions. The most up-to-date information is available on [TU Colab](https://colab.tuwien.ac.at/display/SE/How+to+create+properties). 

### [Versioning](docs/model_versioning.md)
Information about the implementation of model versioning.

### [UXF Parsing](docs/parser.md)
Explanation of how UXF files are parsed into Java domain classes.

### [DSL](docs/dsl.md)
Documentation about how Java domain classes are transformed into a custom DSL for versioning and storing purposes.

### [Neo4j](docs/neo4j.md)
Information regarding the Neo4J integration.

### [Frontend](docs/frontend.md)
Information about the visual frontend part of the application (including requirements & setup).

> **Note:**
>Additional information can be found on the [TU Colab page](https://colab.tuwien.ac.at/display/SE/24WS+ASE+PR+UMLet+Model+Change+Management) of this project.

## Contributors

- Konrad Fabian Aigner
- Benjamin Giraud-Renard
- Lukas Loidolt
- Théo Hauray
- Dániel Hajós
- Andrej Kapusta