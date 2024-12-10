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