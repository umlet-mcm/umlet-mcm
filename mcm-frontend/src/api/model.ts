import axios from "axios"

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

export const deleteModelFromConfig = async (modelId: string): Promise<void> => {
    try {
        await apiClient.delete(`/model/${modelId}`);
    } catch (error) {
        throw error
    }
}