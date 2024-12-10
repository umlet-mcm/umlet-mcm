import axios from "axios"
import {Configuration} from "@/types/Configuration.ts";

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'multipart/form-data'
    }
});

export const uploadUxfToConfiguration = async (change: any): Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", change.target.files[0]);
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

export const uploadUxfToModel = async (change: any, configName: string) : Promise<Configuration> => {
    try {
        let formData = new FormData();
        formData.append("file", change.target.files[0]);
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

export const exportConfigurationToUxf = async (id: string, name: string) => {
    try {
        const response = await apiClient.get(`/files/uxf/export/configuration/${id}`, { responseType: 'blob' });

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

export const exportModelToUxf = async (id: string) => {
    try {
        const response = await apiClient.get('/export/model/' + id);
        return response.data;
    } catch (error) {
        throw error;
    }
}