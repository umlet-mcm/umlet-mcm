# UMLet Model Change Management - 2024WS-ASE-PR

Versioning software for models created in UMLet(ino)

## Features

- Keep track of model versions
- Create configurations from multiple models
- Compare different versions of configurations
- Merge multiple models into a single model
- Export configurations, models and subgraphs as UXF and CSV files
- Run Neo4j queries on the models

## [Building and Running the App](docs/usage.md)
This section provides detailed instructions on setting up and executing the application using Electron.

## Docs
This section contains all documentation related to the code behavior of the application. It is divided into logical sections for easy navigation. Each section leads to a separate documentation file.

### Attribute Conventions

The custom attributes stored in the uxf files follow strict conventions. The most up-to-date information is available on [TU Colab](https://colab.tuwien.ac.at/display/SE/Conventions+for+element+properties). 

### [Versioning](docs/model_versioning.md)
Find the code documentation about versioning packages and modules in this section.

### [UXF Parsing](docs/parser.md)
Discover how parsing from UXF files to Java domain classes is done by following this link.

### [DSL](docs/dsl.md)
Explore the documentation about how Java domain classes are turned into a custom DSL for versioning and storing purposes.

### [Neo4j](docs/neo4j.md)
Learn about the incorporation of Neo4j into the app through this documentation.

### [Frontend](docs/frontend.md)
Access information about the visual frontend part of our application here.

> **Note:**
>Additional information can be found on the [TU Colab page](https://colab.tuwien.ac.at/display/SE/24WS+ASE+PR+UMLet+Model+Change+Management) of this project.

## Contributors

- Konrad Fabian Aigner
- Benjamin Giraud-Renard
- Lukas Loidolt
- Théo Hauray
- Dániel Hajós
- Andrej Kapusta