export type Node = {
    id: string
    name: string
    attributes: [string, string][] // key-value pairs
}

export type Edge = {
    id: string
    name: string | null
    from: string
    to: string
}

export const nodes_data = [
    {
        id: '1',
        name: 'Node 1',
        attributes: [['key1', 'value1'], ['key2', 'value2'], ['key3', 'value3'], ['key4', 'value4'], ['key5', 'value5'], ['key6', 'value6']]
    },
    {
        id: '2',
        name: 'Node 2',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    },
    {
        id: '3',
        name: 'Node 3',
        attributes: [['key1', 'value1']]
    }
]

export const edges_data = [
    {
        id: '1',
        name: 'Edge 1',
        from: '1',
        to: '3'
    }
]