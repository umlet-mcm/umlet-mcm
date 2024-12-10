import axios from "axios"
import {Configuration} from "@/types/Configuration";
import {Model} from "@/types/Model.ts";

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

export const getAllConfigurations = async (): Promise<Configuration[]> => {
    try {
        // const response = await apiClient.get('/configurations'); //todo use api
        // return response.data;
        return configurations_data
    } catch (error) {
        throw error;
    }
};

export const getConfigurationById = async (data: { id: string }): Promise<Configuration> => {
    try {
        // const response = await apiClient.get('/configurations'); //todo use api
        // return response.data;
        return configurations_data[0]
    } catch (error) {
        throw error;
    }
};

export const createConfiguration = async (data: { name: string }): Promise<Configuration> => {
    try {
        const response = await apiClient.post('/configurations', data);
        return {
            name: response.data.name,
            version: response.data.version,
            models: []
        };
    } catch (error) {
        throw error;
    }
};

export const updateConfiguration = async (data: { name: string, models: Model[] }): Promise<Configuration> => {
    //todo need to check if name is unique
    try {
        const response = await apiClient.put(`/configurations/${data.name}`, data);
        return {
            name: data.name,
            version: '',
            models: data.models
        };
    } catch (error) {
        throw error;
    }
};

export const deleteConfiguration = async (data: { name: string }): Promise<void> => {
    try {
        await apiClient.delete(`/configurations/${data.name}`);
    } catch (error) {
        throw error;
    }
};
