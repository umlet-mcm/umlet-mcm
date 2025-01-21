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
 * @return the newly created configuration
 */
export const uploadUxfToConfiguration = async (file: any): Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
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
 * @return the updated configuration
 */
export const uploadUxfToModel = async (file: any, configName: string) : Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", file);
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
 * @param id the id of the configuration to export
 * @param name the name of the configuration to export
 * @param type "configuration" or "model"
 * @return the uxf file to download
 */
export const exportToUxf = async (id: string, name: string, type: string) => {
    try {
        const response = await apiClient.get(`/files/uxf/export/${type}/${id}`, { responseType: 'blob' });

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