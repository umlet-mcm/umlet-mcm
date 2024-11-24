export type Node = {
    id: string
    name: string
    model_id: string
    attributes: [string, string][] // key-value pairs
}

export type Edge = {
    id: string
    name: string | null
    model_id: string,
    from: string
    to: string
}

export const nodes_data = [
    // model 1
    {
        id: '1',
        name: 'Node 1',
        model_id: '1',
        attributes: [['key1', 'value1'], ['key2', 'value2'], ['key3', 'value3'], ['key4', 'value4'], ['key5', 'value5'], ['key6', 'value6']]
    },
    {
        id: '2',
        name: 'Node 2',
        model_id: '1',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    },
    {
        id: '3',
        name: 'Node 3',
        model_id: '1',
        attributes: [['key1', 'value1']]
    },
    // model 2
    {
        id: '1',
        name: 'GHG Input Product',
        model_id: '2',
        attributes: [['key1', 'value1'], ['key2', 'value2'], ['key3', 'value3'], ['key4', 'value4'], ['key5', 'value5'], ['key6', 'value6']]
    },
    {
        id: '2',
        name: 'GHG Process Activity',
        model_id: '2',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    },
    {
        id: '3',
        name: 'GHG Output Product',
        model_id: '2',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    },
    {
        id: '4',
        name: 'GHG Resource Equipment',
        model_id: '2',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    },
    {
        id: '5',
        name: 'GHG Resource Energy Source',
        model_id: '2',
        attributes: [['key1', 'value1'], ['key2', 'value2']]
    }
]

export const edges_data = [
    {
        id: '1',
        name: 'Edge 1',
        model_id: '1',
        from: '1',
        to: '3'
    },
    // model 2
    {
        id: '1',
        name: 'Input',
        model_id: '2',
        from: '1',
        to: '2'
    },
    {
        id: '2',
        name: 'Output',
        model_id: '2',
        from: '2',
        to: '3'
    },
    {
        id: '3',
        name: 'Resource',
        model_id: '2',
        from: '2',
        to: '4'
    },
    {
        id: '4',
        name: 'Resource',
        model_id: '2',
        from: '4',
        to: '2'
    },
    {
        id: '5',
        name: 'Energy',
        model_id: '2',
        from: '5',
        to: '4'
    },
]