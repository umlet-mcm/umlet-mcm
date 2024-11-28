export type Node = {
    id: string
    text: string
    type: string
    relations: Relation[]
    labels: string[]
    properties: [string, string][] // key-value pairs
}

export type Relation = {
    type: string
    text: string | undefined
    source: string // node id
    target: string // node id
}