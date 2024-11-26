import axios from "axios"
import {Configuration, configurations_data} from "@/datamodel/Configuration";

const apiClient = axios.create({
    baseURL: 'http://localhost/api', //todo change to api url
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

export const createConfiguration = async (data: { projectName: string }): Promise<Configuration> => {
    try {
        // const response = await apiClient.post('/configurations', data); //todo use api
        // return response.data;
        return configurations_data[0]
    } catch (error) {
        throw error;
    }
};