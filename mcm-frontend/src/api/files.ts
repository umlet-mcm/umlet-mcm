import axios from "axios"
import {Configuration} from "@/types/Configuration.ts";
import { AppConfig } from "@/config";

const apiClient = axios.create({
    baseURL: AppConfig.apiBaseUrl + '/api/v1',
    headers: {
        'Content-Type': 'multipart/form-data'
    }
});

/**
 * Upload uxf file to the server and create a new configuration
 * @param file the uxf file to upload
 * @param name the name of the configuration to create
 * @param version the version of the configuration to create
 * @return the newly created configuration
 */
export const uploadUxfToConfiguration = async (file: any, name: string | undefined, version: string | undefined): Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
        if(name) formData.append("name", name);
        if(version) formData.append("version", version);
        else formData.append("version", "v1.0.0");
        const response = await apiClient.post(
            '/files/uxf',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * Upload uxf file to the server and add it to an existing configuration
 * @param file the uxf file to upload
 * @param configName the name of the configuration to add the file to
 * @param name the name of the model to add
 * @return the updated configuration
 */
export const uploadUxfToModel = async (file: any, configName: string, name: string | undefined) : Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
        if(name) formData.append("modelName", name);
        const response = await apiClient.post(
            `/files/uxf/${configName}`,
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * Export a configuration or a model to uxf file
 * @param id the id or name of the configuration to export
 * @param name the name of the configuration to export
 * @param type "configuration" or "model"
 * @return the uxf file to download
 */
export const exportToUxf = async (id: string, name: string, type: string) => {
    try {
        const req = type === "configuration" ? `/files/uxf/export/configuration/${id}` : `/files/uxf/export/configuration/${id}/model/${name}`;
        const response = await apiClient.get(req, { responseType: 'blob' });

        const blob = new Blob([response.data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = `${name}.uxf`;
        document.body.appendChild(link);
        link.click();

        document.body.removeChild(link);
    } catch (error) {
        throw error;
    }
}

export const uploadUxfConfiguration = async (file: any, configName: string): Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
        formData.append("name", configName);
        formData.append("version", "v1.0.0");
        const response = await apiClient.post(
            '/files/uxf',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const updateConfiguration = async (file: any, configName: string, version: string | undefined): Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
        formData.append("name", configName);
        if (version) formData.append("version", version);
        const response = await apiClient.put(
            '/files/uxf/' + configName,
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return response.data;
    } catch (error) {
        throw error;
    }
}