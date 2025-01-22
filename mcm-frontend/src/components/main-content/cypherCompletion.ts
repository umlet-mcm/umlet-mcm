import { CompletionContext, CompletionResult } from '@codemirror/autocomplete'

const keywords = [
    'MATCH', 'RETURN', 'WHERE', 'CREATE', 'DELETE', 'REMOVE',
    'SET', 'ORDER BY', 'SKIP', 'LIMIT', 'MERGE', 'OPTIONAL MATCH',
    'WITH', 'UNWIND', 'DISTINCT', 'CASE', 'WHEN', 'THEN', 'ELSE',
    'END', 'CREATE UNIQUE', 'FOREACH', 'ON', 'DETACH DELETE', 'AND', 'OR', 'CONTAINS'
]

const functions = [
    'count', 'collect', 'sum', 'avg', 'max', 'min',
    'toString', 'toInteger', 'toFloat', 'type', 'size',
    'length', 'nodes', 'relationships', 'labels', 'keys',
    'exists', 'id', 'coalesce', 'timestamp', 'startNode',
    'endNode', 'properties'
]

const macros = [
    {'Get_everything': 'MATCH (n) RETURN n'},
    {'Get_all_nodes': 'MATCH (n:Node) RETURN n'},
    {'Get_all_models': 'MATCH (n:Model) RETURN n'},
    {'Get_all_relations': 'MATCH ()-[r:RELATION]->() RETURN r'},
    {'Get_all_labels': 'CALL db.labels()'},
    {'Get_all_relationship_types': 'CALL db.relationshipTypes()'},
    {'Get_database_schema': 'CALL db.schema.visualization()'},
    {'Find_node_by_name': 'MATCH (n:Node) WHERE n["name"] CONTAINS "(part of) node-name" RETURN n'},
    {'Find_node_by_user_defined_property (single value)': 'MATCH (n:Node) WHERE n["properties.propertyName"] = "value" RETURN n'},
    {'Find_node_by_user_defined_property (list)': 'MATCH (n:Node) WHERE n["properties.propertyName"] CONTAINS "value" RETURN n'},
    {'Find_node_by_Umlet_property (e.g. background)': 'MATCH (n:Node) WHERE n["umletProperties.bg"] = "#ffffff" RETURN n'},
    {'Find_relation_by_name': 'MATCH ()-[r:RELATION]->() WHERE r["name"] CONTAINS "(part of) relation-name" RETURN r'},
    {'Find_duplicate_nodes': 'MATCH (n:Node) WITH n["properties.propertyName"] AS key, COUNT(n) AS count WHERE count > 1 RETURN key, count'},
    {'Find_shortest_path_between_two_nodes': 'MATCH p = shortestPath((a)-[*]->(b)) WHERE a["properties.propertyName"] = "value1" AND b["properties.propertyName"] = "value2" RETURN p'},
    {'Count_everything': 'MATCH (n) RETURN COUNT(n)'},
    {'Count_models': 'MATCH (m:Model) RETURN COUNT(m) as ModelCount'},
    {'Count_nodes': 'MATCH (n:Node) RETURN COUNT(n) as NodeCount'},
    {'Count_relations': 'MATCH ()-[r:RELATION]->() RETURN COUNT(r) as RelationCount'},
    {'Sum_up_all_costs': 'CALL apoc.periodic.commit(\n' +
            '\'MATCH (n)\n' +
            'WHERE n["properties.cost"] = -1\n' +
            'WITH n LIMIT $limit\n' +
            'MATCH (n)<-[r:RELATION]-(predecessor)\n' +
            'WITH n, COLLECT(predecessor) AS predecessors, COLLECT(r) AS relations\n' +
            'WHERE ALL(predecessor IN predecessors WHERE predecessor["properties.cost"] <> -1)\n' +
            'UNWIND range(0, size(predecessors) - 1) AS idx\n' +
            'WITH n, predecessors[idx] AS predecessor, relations[idx] AS rel\n' +
            'WITH n, SUM(predecessor["properties.cost"] * rel["properties.cost"]) AS summedCost\n' +
            'SET n["properties.cost"] = summedCost\n' +
            'RETURN COUNT(*)\',\n' +
            '{limit: 1000}\n' +
    ')'},
    {'Remove_properties': 'MATCH (n:LabelName) REMOVE n["properties.propertyName"] RETURN n'},
    {'Add_an_index': 'CREATE INDEX index_name FOR (n:LabelName) ON (n["properties.propertyName"])'},
    {'Drop_an_index': 'DROP INDEX index_name'},
];

export function cypherCompletion(context: CompletionContext): CompletionResult | null {
    const word = context.matchBefore(/\w*/)
    if (!word) return null

    return {
        from: word.from,
        options: [
            ...keywords.map(keyword => ({
                label: keyword,
                type: 'keyword'
            })),
            ...functions.map(func => ({
                label: func,
                type: 'function'
            })),
            ...macros.map(mac => ({
                label: Object.keys(mac)[0],
                apply: Object.values(mac)[0],
                type: 'text',
                detail: 'macro'
            })),
        ]
    }
}