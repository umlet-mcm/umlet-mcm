import axios from "axios"
import {Model} from "@/types/Model.ts";

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

export const mergeModels = async (models: Model[], outName: string): Promise<Model> => {
    try {
        // const data = {
        //     models: models,
        //     outName: outName
        // }
        // const response = await apiClient.post('/models/merge', data); //todo use api
        // return response.data;
        return {
            id: outName,
            nodes: models.flatMap(model => model.nodes)
        }
    } catch (error) {
        throw error;
    }
};