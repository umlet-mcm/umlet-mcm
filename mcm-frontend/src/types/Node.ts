export type Node = {
    id: string
    elementType: string
    tags: string[]
    originalText: string
    title: string
    description: string
    mcmModel: string
    mcmModelId: string
    mcmAttributes: Record<string, object>
    umletAttributes: Record<string, string>
    pprType: string
    generatedAttributes: number[]
    relations: Relation[]
}

export type Relation = {
    type: string
    target: string
    id: string
    tags: string[]
    originalText: string
    title: string
    description: string
    mcmModel: string
    mcmModelId: string
    mcmAttributes: Record<string, object>
    umletAttributes: Record<string, string>
    pprType: string
}