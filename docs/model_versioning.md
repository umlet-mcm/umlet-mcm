# Model Versioning

Models are converted to an XML-based DSL and versioned in Git repositories.

There is one repository per configuration and a single configuration may contain multiple models with each model
holding multiple nodes and relations between those nodes.

We use `JGit` - a pure Java implementation of Git - to interact with the repositories.
The code for this can be found in the `git` package in `mcm-core` (a Gradle subproject of `mcm-backend`).

## Versioning REST endpoints

The `ConfigurationController` in `mcm-server` provides a variety of REST endpoints
for interacting with configurations. The supported workflows are
- creating repositories
- saving configurations to repositories, thereby creating Git commits
- checking out specific versions of configurations
- comparing different versions of configurations via a `git diff` of their XML files
- resetting the working directory of configurations to specific versions


Note that a lot of workflows - particularly all UMLet related ones - go through the UXF file endpoints, which internally make use
of the `ConfigurationService` and the `git` package.
I.e., users may want to create new configurations by uploading (and thus parsing) UXF files which is supported by the UXF controller.

- `GET /api/v1/configurations/{name}`
    - Retrieves the **currently checked out** version (as in `git checkout`) of the configuration from its repository
    - Query parameter `loadIntoGraphDB` (boolean, default false)
        - If true, the configuration is loaded into the `Neo4J` graph database
    
- `GET /api/v1/configurations`
  - Retrieves all configurations in their currently checked out version

- `POST /api/v1/configurations`
  - takes in a `ConfigurationDTO`, creates the repository for a new configuration and saves the configuration to it
  - Note that the configuration name must be unique among all configurations in the application instance
  - Query parameter `loadIntoGraphDB` (boolean, default true)
    - If true, the configuration is loaded into the `Neo4J` graph database

- `PUT /api/v1/configurations`
  - takes in a `ConfigurationDTO`, updates the configuration in its repository
  - this means creating a new version of the configuration, i.e., a new commit in its Git repository
  - the contents of the `ConfigurationDTO` sent to this endpoint will replace the working directory of the repository
    - meaning that the new version will contain only the models and nodes specified in the `ConfigurationDTO`
  - Query parameter `loadIntoGraphDB` (boolean, default true)
    - If true, the configuration is loaded into the `Neo4J` graph database

- `DELETE /api/v1/configurations/{name}`
  - Deletes the configuration with the given name
  - I.e., it deletes its Git repository

- `GET /api/v1/configurations/{name}/versions/{version}`
  - Retrieves a specific version of the configuration from its repository
  - The version is given by a unique identifier
    - the Git commit hash
    - a version name (Git tag) which was auto-generated or user-supplied when creating the version
  - Query parameter `loadIntoGraphDB` (boolean, default false)
    - If true, the configuration is loaded into the `Neo4J` graph database

- `GET /api/v1/configurations/{name}/versions`
  - Retrieves all versions of the configuration from its repository
  - Returns `ConfigurationVersionDTO` objects containing
    - the Git commit hash of the version
    - the auto-generated name of the version (e.g., `v1.0.0`)
    - the user-supplied name of the version if the user supplied one when the version was created

- `GET /api/v1/configurations/{name}/versions/{newVersion}/compare/{oldVersion}`
  - Compares two different versions of a configuration via `git diff`
  - returns diff entries containing 
    - the ID of the compared objects
    - the title of the compared objects
      - the new title if one is set for and differs between both objects
    - the diff type (ADD, DELETE, MODIFY)
    - the diff content (including Git headers and hunks)
  - Query parameter: includeUnchanged (boolean, default false)
    - whether to also include "diff entries" for unchanged objects in the HTTP response 

- `POST /api/v1/configurations/{name}/versions/{version}/checkout`
  - Checks out a specific version of the configuration from its repository
  - Note that this will check out a branch if one references the given version
  - Otherwise, it will directly check out the version specifier in DETACHED HEAD state
  - Query parameter: loadIntoGraphDB (boolean, default false)
    - If true, the configuration is loaded into the `Neo4J` graph database in the newly checked out version
  
- `POST /api/v1/configurations/{name}/versions/{version}/reset`
  - Resets the working directory of the configuration to the given version
  - Query parameter: loadIntoGraphDB (boolean, default false)
    - If true, the configuration is loaded into the `Neo4J` graph database in the newly reset version

- `PUT /api/v1/configurations/{name}/rename`
  - Renames a configuration
  - This is necessary because names are the unique identifiers of configurations
  - So we can't just update the name through the regular PUT endpoint
  - Query parameter: newName (string)

# Peculiarities of the implementation

Commits always extend the `main` branch. If the repository HEAD is detached, the new commit updates `main` instead of the HEAD.
This was done because more advanced branching logic was out of scope for this project and we therefore wanted to avoid
the "accidental" creation of different branches by updating, e.g., a detached HEAD.

Git checkout can check out any version. However, if there is a branch pointing to the specified version, the branch is checked out instead of the version itself.

Version names are implemented as Git tags. We DO allow spaces in version names. However, as Git does not,
these are URL-encoded in the actual git tag names, along with a few other special characters.

# Configuration validation restrictions

All elements of a configuration must have unique IDs. IDs also serve as the unique file names of the configuration elements saved
to the repository.

The configuration name must be a valid folder name, i.e., it cannot include forward or backward slashes nor other special characters or reserved keywords that aren't allowed by the file system

Each node and relation has a model ID attribute referencing the model that contains it. This attribute can be `null` in which case it is set by the backend.
However, if it is set it must be set to the ID of the model that actually contains the node or relation.
Otherwise, an exception will be thrown.

All validation constraints are enforced in the service layer of the application.

# Implementation details

Most of the relevant implementation can be found in the `git` subpackage in `mcm-core`.
Of particular interest is the `infrastructure` folder inside that package, as it contains all interactions with `Jgit`.
The `Jgit` API is quite-cumbersome. `infrastructure` contains wrappers and data objects to (hopefully) provide a nicer API for the rest of the application.

While `infrastructure` is domain-agnostic, `operation` knows about domain models like `Configuration`, `Model`, `Node`, and `Relation`.
It is responsible (along with the `DSLTransformer` logic from the `core` package) for converting these elements to and from their XML representation.

