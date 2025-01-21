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
    {'ALL_NODES': 'MATCH (n) RETURN n'},
    {'ALL_RELATIONS': 'MATCH ()-[r]->() RETURN r'},
    {'ALL_LABELS': 'CALL db.labels()'},
    {'ALL_REL_TYPES': 'CALL db.relationshipTypes()'},
    {'FIND_BY_LABEL': 'MATCH (n:LabelName) RETURN n'},
    {'FIND_BY_PROPERTY': 'MATCH (n) WHERE n.propertyName = "value" RETURN n'},
    {'FIND_RELATED_NODES': 'MATCH (n)-[r:RELATION_TYPE]->(m) WHERE n.propertyName = "value" RETURN m'},
    {'CREATE_NODE': 'CREATE (n:LabelName {propertyName: "value"}) RETURN n'},
    {'CREATE_RELATION': 'MATCH (a:LabelA), (b:LabelB) WHERE a.propertyName = "value1" AND b.propertyName = "value2" CREATE (a)-[r:RELATION_TYPE]->(b) RETURN r'},
    {'DELETE_NODE': 'MATCH (n:LabelName) WHERE n.propertyName = "value" DELETE n'},
    {'DELETE_RELATION': 'MATCH ()-[r:RELATION_TYPE]->() DELETE r'},
    {'COUNT_NODES': 'MATCH (n) RETURN COUNT(n)'},
    {'COUNT_RELATIONS': 'MATCH ()-[r]->() RETURN COUNT(r)'},
    {'FIND_SHORTEST_PATH': 'MATCH p = shortestPath((a)-[*]->(b)) WHERE a.propertyName = "value1" AND b.propertyName = "value2" RETURN p'},
    {'GET_SCHEMA': 'CALL db.schema.visualization()'},
    {'FIND_DUPLICATE_NODES': 'MATCH (n:LabelName) WITH n.propertyName AS key, COUNT(n) AS count WHERE count > 1 RETURN key, count'},
    {'REMOVE_PROPERTY': 'MATCH (n:LabelName) REMOVE n.propertyName RETURN n'},
    {'ADD_INDEX': 'CREATE INDEX index_name FOR (n:LabelName) ON (n.propertyName)'},
    {'DROP_INDEX': 'DROP INDEX index_name'},
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