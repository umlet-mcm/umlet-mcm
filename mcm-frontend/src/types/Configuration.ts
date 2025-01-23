import {Model} from "@/types/Model.ts";

export type Configuration = {
    name: string
    version: Version,
    models: Model[]
}

export type Version = {
    hash: string,
    name: string,
    customName: string
}