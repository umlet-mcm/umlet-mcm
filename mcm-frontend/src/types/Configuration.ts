import {Model} from "@/types/Model.ts";

export type Configuration = {
    name: string
    models: Model[]
}

export const configurations_data = [
    {
        name: 'Configuration1',
        models: [
            {
                id: 'Model1',
                nodes: [
                    {
                        id: 'Node1',
                        text: 'Node 1',
                        type: 'type1',
                        properties: [['key1','val1'],['key2','val2']] as [string, string][],
                        labels: ['label1'],
                        relations: [
                            {
                                type: 'relation1',
                                text: 'relation1',
                                source: 'Node1',
                                target: 'Node2'
                            }
                        ]
                    },
                    {
                        id: 'Node2',
                        text: 'Node 2',
                        type: 'type2',
                        properties: [['key1','val1']] as [string, string][],
                        labels: ['label2'],
                        relations: []
                    },
                    {
                        id: 'Node3',
                        text: 'Node 3',
                        type: 'type3',
                        properties: [['key1','val1']] as [string, string][],
                        labels: ['label3'],
                        relations: []
                    }
                ]
            },
            {
                id: 'Model2',
                nodes: []
            }
        ],
    },
]