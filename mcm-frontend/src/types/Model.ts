import {Node} from "@/types/Node.ts";

export type Model = {
    id: string
    nodes: Node[]
    tags: string[]
    originalText: string
    title: string
    description: string
    mcmAttributes: Record<string, object>
    zoomLevel: 10 | number
}