import axios from "axios"
import {Configuration} from "@/types/Configuration";
import {Model} from "@/types/Model.ts";

const apiClient = axios.create({
    baseURL: '/api/v1/configurations',
    headers: {
        'Content-Type': 'application/json'
    }
});

export const getAllConfigurations = async (): Promise<Configuration[]> => {
    try {
        const response = await apiClient.get('');
        return response.data.sort((a: Configuration, b: Configuration) => a.name.localeCompare(b.name));
    } catch (error) {
        throw error;
    }
};

export const getConfigurationById = async (data: { id: string }): Promise<Configuration> => {
    try {
        const response = await apiClient.get(`/${data.id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const createConfiguration = async (data: { name: string }): Promise<Configuration> => {
    try {
        const response = await apiClient.post('', data);
        return {
            name: response.data.name,
            version: response.data.version,
            models: []
        };
    } catch (error) {
        throw error;
    }
};

export const updateConfiguration = async (data: { name: string, version: string, models: Model[] }): Promise<Configuration> => {
    try {
        const response = await apiClient.put('', data);
        return response.data
    } catch (error) {
        throw error;
    }
};

export const deleteConfiguration = async (data: { name: string }): Promise<void> => {
    try {
        await apiClient.delete(`/${data.name}`);
    } catch (error) {
        throw error;
    }
};
