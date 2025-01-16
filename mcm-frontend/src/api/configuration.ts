import axios from "axios"
import {Configuration} from "@/types/Configuration";
import {Model} from "@/types/Model.ts";

const apiClient = axios.create({
    baseURL: '/api/v1/configurations',
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * Get all configurations from the server
 * @return a list of all configurations
 */
export const getAllConfigurations = async (): Promise<Configuration[]> => {
    try {
        const response = await apiClient.get('');
        return response.data.sort((a: Configuration, b: Configuration) => a.name.localeCompare(b.name));
    } catch (error) {
        throw error;
    }
};

/**
 * Get a configuration by its id
 * @param data the id of the configuration to retrieve
 * @return the configuration with the given id
 */
export const getConfigurationById = async (data: { id: string }): Promise<Configuration> => {
    try {
        const response = await apiClient.get(`/${data.id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * Create a new configuration from scratch
 * @param data the name of the configuration to create
 * @return the newly created configuration
 */
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

/**
 * Update an existing configuration
 * @param data the new configuration data
 * @return the updated configuration
 */
export const updateConfiguration = async (data: { name: string, version: string, models: Model[] }): Promise<Configuration> => {
    try {
        const response = await apiClient.put('', data);
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * Delete a configuration by its name
 * @param data the name of the configuration to delete
 */
export const deleteConfiguration = async (data: { name: string }): Promise<void> => {
    try {
        await apiClient.delete(`/${data.name}`);
    } catch (error) {
        throw error;
    }
};

/**
 * Get all versions of a configuration
 * @param name the name of the configuration to retrieve versions for
 * @param version //todo need to be removed after api is implemented
 * @return a list of all versions of the configuration
 */
export const getConfigurationVersions = async (name: string, version: string): Promise<string[]> => {
    try {
        // const response = await apiClient.get(`/${name}/versions`);
        // return response.data;
        // todo retrieve using only the api. the second argument is just to avoid blank return
        console.log("Getting versions for " + name + " " + version);
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
        console.log("Comparing " + name + " " + version1 + " " + version2);
        return "";
    } catch (error) {
        throw error;
    }
}
