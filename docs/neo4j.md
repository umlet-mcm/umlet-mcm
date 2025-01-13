# Neo4j Database

## Database Content
The database has at a given time only one configuration loaded. This happens when a new UXF file is uploaded, creating either new Configuration or updating an existing one. 

An alternative way to change the database can be by calling an endpoint ```POST /api/v1/graphdb/configuration/{configurationID}``` where the ```configurationID``` is an ID of a configuration that has already been stored in a repository.

There is also a possibility to delete the content of database entirely by calling ```DELETE /api/v1/graphdb/```. This will delete the database irreversibly and any unsaved changes will be lost.

## Capabilities
User can use the database to perform following actions:

### Cypher Queries
The database (configuration) can be queried by Cypher queries (commands). These queries can modify configuration properties, select specific nodes, create new projections, and much more. To send a query, use the endpoint ```POST /api/v1/graphdb/query```. The structure for this endpoint is as follows:
```json
{
    "query":"MATCH(n) RETURN n"
}
```
### Save the Configuration
The database supports saving its content to a GIT repository, creating a new version of the configuration. However, this feature does not support adding or deleting nodes (such as Node, Configuration, Model, or Relation). Instead, it focuses on editing, creating, and deleting properties. To save the configuration, send a request to ```POST /api/v1/graphdb/save``` with an empty body.

### CSV Export
Exporting the database or a subgraph to a CSV file is also available. This feature is particularly useful for loading data into table processors like Tidyverse or MS Office Excel. To export the entire database, send a request to ```GET /api/v1/graphdb/csvExport``` with the request parameter ```fileName```. This will return the export as a downloadable file named ```fileName.csv```.

If exporting a subgraph created by a Cypher query is preferred, send a request to ```POST /api/v1/graphdb/csvExport``` with the request parameter ```fileName``` and the following body structure:

```json
{
    "query":"MATCH(n) RETURN n"
}
```
The returned file will be named ```fileName.csv``` and will containt the resulting subgraph of the provided query.

### UXF Export
The database also supports exporting a subset of nodes to a UXF file. To do this, send a request to ```POST /api/v1/graphdb/queryExport``` with the request parameter ```fileName```. The body must contain a query that returns either Nodes or IDs of the Nodes to be exported.

Here is an example of a request body with a query that returns Nodes:
```json
{
    "query":"MATCH(n:Node) RETURN n"
}
```
Example of a request body with query conforming to the second variant can be:
```json
{
    "query":"MATCH(n:Node) RETURN elementId(n)"
}
```
In the second variant the name of the column does not matter and can even be renamed. The result is returned as a new Model exported in a UXF file named ```fileName.uxf```

## Plugins
The database contains two additional plugins that can be used in a query. The first one **APOC** (Awesome Procedures on Cypher) is a plugin that adds various procedures and functions. For its utilisation please see its documentation.

The second plugin is named **GDS** (Graph Data Science) and enhances work with graphs by providing many useful graph algorithms.

## Domain Entities
There are several domain entities that represent the configuration in a database

+ **ConfigurationEntity:** class that holds all data about a single Configuration and all the Models that are connected to the given Configuration.
+ **ModelEntity:** class contains all properties of a Model together with all the Nodes that belong to one Model.
+ **NodeEntity:** class containing all properties of a Node together with all its connections (relations) to other Nodes.
+ **RelationEntity:** class that contains data about a Relation between two Nodes.

## Obtaining Data from Dataabase
To obtain data from a database and convert them to entities several DAOs are provided. Moreover to support a generic Cypher query the class **RawNeo4jService** connects directly to the database socket, inits a session, sends Cypher queries and obtains raw data which then sends to process in other classes.

## Database Configuration
The database contains one class named **Neo4JProperties** which contains generic properties set at the launch. Then there is a folder named **configs** located in resources which contains files for configuration of a database and its plugins. Those are copied at launch, too.

The **Neo4JConfig** class copies plugins, configs, launches a DB and connects to it when its ready.