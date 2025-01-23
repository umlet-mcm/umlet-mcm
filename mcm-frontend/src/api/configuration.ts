import axios from "axios"
import {Configuration, Version} from "@/types/Configuration";
import {DiffObject} from "@/types/DiffObject.ts";
import {AppConfig} from "@/config";


const apiClient = axios.create({
    baseURL: AppConfig.apiBaseUrl + '/api/v1/configurations',
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
export const updateConfiguration = async (data: Configuration): Promise<Configuration> => {
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
 * @return a list of all versions of the configuration
 */
export const listConfigurationVersions = async (name: string): Promise<Version[]> => {
    try {
        const response = await apiClient.get(`/${name}/versions`);
        return response.data;
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
export const compareTwoVersions = async (name: string, version1: string, version2: string): Promise<DiffObject[]> => {
    try {
        const response = await apiClient.get(`/${name}/versions/${version1}/compare/${version2}`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

/**
 * Get a specific configuration by its name and version
 * @param name
 * @param version
 */
export const getConfigurationVersion = async (name: string, version: string): Promise<Configuration> => {
    try {
        const response = await apiClient.get(`/${name}/versions/${version}`);
        return response.data
    } catch (error) {
        throw error;
    }
}

/**
 * Checkout a specific configuration by its name and version
 * @param name
 * @param version
 */
export const checkoutConfiguration = async (name: string, version: string): Promise<Configuration> => {
    try {
        const response = await apiClient.post(`/${name}/versions/${version}/checkout`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

/**
 * Reset a specific configuration by its name and version
 * @param name
 * @param version
 */
export const resetConfiguration = async (name: string, version: string): Promise<Configuration> => {
    try {
        const response = await apiClient.post(`/${name}/versions/${version}/reset`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const renameConfiguration = async (name: string, newName: string): Promise<Configuration> => {
    try {
        const response = await apiClient.put(`/${name}/rename`, null, { params: { newName } });
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const getLastCreatedConfiguration = async (name: string, currentVersion: Version): Promise<Configuration> => {
    try {
        const versionList = await listConfigurationVersions(name)
        const lastVersion = versionList[0]
        return checkoutConfiguration(name, lastVersion.hash !== currentVersion.hash ? lastVersion.hash : currentVersion.hash)
    } catch (error) {
        throw error;
    }
}