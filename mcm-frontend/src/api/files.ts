import axios from "axios"
import {Configuration} from "@/types/Configuration.ts";

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'multipart/form-data'
    }
});

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