import {Model} from "@/datamodel/Model.ts";

export type Configuration = {
    id: string
    name: string
    path: string
    models: Model[]
}

export const configurations_data = [
    {
        id: '1',
        name: 'Configuration 1',
        path: 'path/to/configuration1',
        models: [
            {
                id: 1,
                name: 'Model 1'
            },
            {
                id: 2,
                name: 'Model 2'
            }
        ],
    },
]