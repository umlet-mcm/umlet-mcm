import { Model } from "@/types/Model.ts";
import { Node } from "@/types/Node.ts";
import { sendRequest } from "@/api/graphDB.ts";

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
    if(nodes.length) await addRelationsToNodes(nodes);
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

function createNodeFromResponse(rawNode: any): Node {
    return {
        id: rawNode.elementId,
        title: rawNode.properties.name.val,
        elementType: "UMLClass",
        tags: rawNode.properties.tags.values,
        originalText: rawNode.properties.name.val,
        description: rawNode.properties.description.val,
        generatedAttributes: [],
        pprType: "",
        mcmModel: "",
        mcmModelId: "",
        relations: [],
        umletPosition: {
            x: rawNode.properties["position.x"].val,
            y: rawNode.properties["position.y"].val,
            width: rawNode.properties["position.width"].val,
            height: rawNode.properties["position.height"].val,
        },
        umletAttributes: extractAttributes(rawNode.properties, "umletProperties"),
        mcmAttributes: extractAttributes(rawNode.properties, "properties"),
    };
}

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

function extractAttributes(obj: Record<string, any>, prefix: string): Record<string, any> {
    return Object.entries(obj)
        .filter(([key]) => key.startsWith(prefix))
        .reduce((result, [key, value]) => {
            const keyWithoutPrefix = key.split('.')[1];
            result[keyWithoutPrefix] = value.val;
            return result;
        }, {} as Record<string, any>);
}
