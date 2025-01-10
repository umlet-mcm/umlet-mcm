import { Model } from "@/types/Model.ts";
import { Node } from "@/types/Node.ts";
import { sendRequest } from "@/api/graphDB.ts";

/**
 * Parses the nodes from the query response and reconstruct a model.
 * @param response The response from the query
 * @param selectedModel The model that was used to generate the query
 * @return The reconstructed model with the nodes and relations from the response
 */
export async function parseResponseGraph(response: Record<string, any>[], selectedModel: Model): Promise<Model> {
    // Detect nodes in the response
    const detectedNodes = detectNodes(response);

    const nodes: Node[] = [];
    detectedNodes.forEach((rawNode: any) => {
        if (nodes.some((n) => n.id === rawNode.elementId)) return;
        if (selectedModel.nodes.some((n) => n.id === rawNode.properties.generatedID.val))
            nodes.push(createNodeFromResponse(rawNode));
    });

    // Fetch relations for the retrieved nodes
    await addRelationsToNodes(nodes);
    return {
        id: "RequestModel",
        nodes: nodes,
        description: "",
        originalText: "",
        tags: [],
        title: "",
        mcmAttributes: {},
    };
}

/**
 * Creates a node object from the raw node data.
 * @param rawNode
 */
function createNodeFromResponse(rawNode: any): Node {
    return {
        id: rawNode.elementId,
        title: rawNode.properties.name.val,
        elementType: "Node",
        tags: rawNode.properties.tags.values,
        originalText: rawNode.properties.name.val,
        description: rawNode.properties.description.val,
        generatedAttributes: [],
        pprType: "",
        mcmModel: "",
        mcmModelId: "",
        relations: [],
        umletAttributes: extractAttributes(rawNode.properties, "umletProperties"),
        mcmAttributes: extractAttributes(rawNode.properties, "properties"),
    };
}

/**
 * Fetches the relations for the retrieved nodes and adds them to the node objects.
 * @param nodes The nodes for which the relations should be fetched
 */
async function addRelationsToNodes(nodes: Node[]) {
    const nodeIds = nodes.map((n) => `"${n.id}"`).join(",");
    // The query fetches all relations between the retrieved nodes by their IDs
    const query = `MATCH (n)-[r]->(m) WHERE elementId(n) IN [${nodeIds}] AND elementId(m) IN [${nodeIds}] RETURN r`;
    const relations = await sendRequest(query);

    // Add the fetched relations to the source node, if they are not already present
    relations.forEach((relation) => {
        const sourceNode = nodes.find((n) => n.id === relation.r.startElementId);
        if (!sourceNode || sourceNode.relations.some((r) => r.id === relation.elementId)) return;
        sourceNode.relations.push(createRelationFromResponse(relation.r));
    });
}

/**
 * Creates a relation object from the raw relation data.
 * @param relation
 */
function createRelationFromResponse(relation: any) {
    return {
        id: relation.elementId,
        type: relation.type,
        title: relation.properties.name.val,
        target: relation.endElementId,
        description: relation.properties.description.val,
        tags: relation.properties.tags.values,
        originalText: relation.properties.name.val,
        pprType: "",
        mcmModel: "",
        mcmModelId: "",
        umletAttributes: extractAttributes(relation.properties, "umletProperties"),
        mcmAttributes: extractAttributes(relation.properties, "properties"),
    };
}

/**
 * Detects the nodes in the response and returns them as an array.
 * @param response The response from the query
 * @return An array of raw node objects
 */
function detectNodes(response: Record<string, any>[]): any[] {
    const nodes: any[] = [];
    for (let item of response) {
        for (let key in item) {
            const obj = item[key];
            if (typeof obj === "object") {
                if (obj.nodes) {
                    // some queries return nodes in an array
                    obj.nodes.forEach((n: Object) => nodes.push(n));
                } else if (obj.labels && obj.labels.includes("Node")) {
                    // some queries return nodes as objects
                    nodes.push(obj);
                }
            }
        }
    }
    return nodes;
}

/**
 * Extracts attributes from the object that have a specific prefix.
 * @param obj The object from which the attributes should be extracted
 * @param prefix The prefix that the attributes should have
 * @return An object with the extracted attributes
 */
function extractAttributes(obj: Record<string, any>, prefix: string): Record<string, any> {
    // Filter the object for keys that start with the given prefix and extract the values
    return Object.entries(obj)
        .filter(([key]) => key.startsWith(prefix))
        .reduce((result, [key, value]) => {
            const keyWithoutPrefix = key.split('.')[1];
            result[keyWithoutPrefix] = value.val;
            return result;
        }, {} as Record<string, any>);
}
