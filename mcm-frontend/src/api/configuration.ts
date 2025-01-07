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
    //todo need to check if name is unique
    try {
        const response = await apiClient.put('', data);
        return response.data;
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

export const getConfigurationVersions = async (name: string, version: string): Promise<string[]> => {
    try {
        // const response = await apiClient.get(`/${name}/versions`);
        // return response.data;
        // todo retrieve using only the api. the second argument is just to avoid blank return
        return [version, "1.0.0", "1.0.1", "1.0.2"];
    } catch (error) {
        throw error;
    }
}

/**
 * Compare two versions of a configuration
 * @param name the name of the configuration to compare
 * @param version1 the first version to compare
 * @param version2 the second version to compare
 */
export const compareTwoVersions = async (name: string, version1: string, version2: string): Promise<string> => {
    try {
        // const response = await apiClient.get(`/${name}/compare/${version1}/${version2}`);
        // return response.data;
        //todo use api to compare
        return "The two versions are the same";
    } catch (error) {
        throw error;
    }
}
