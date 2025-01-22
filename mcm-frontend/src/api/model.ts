import axios from "axios"
import { AppConfig } from "@/config";
import {Model} from "@/types/Model.ts";

const apiClient = axios.create({
    baseURL: AppConfig.apiBaseUrl + '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * Delete a model from a configuration
 * @param modelId the id of the model to delete
 * @param configId the id of the configuration the model belongs to
 */
export const deleteModelFromConfig = async (modelId: string): Promise<void> => {
    try {
        await apiClient.delete(`/model/${modelId}`);
    } catch (error) {
        throw error
    }
}

export const alignModels = async (models: Model[]): Promise<Model[]> => {
    try {
        const response = await apiClient.post('/model/alignModels', models);
        return response.data
    } catch (error) {
        throw error
    }
}