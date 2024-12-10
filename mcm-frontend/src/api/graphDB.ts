import axios from "axios"

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

export const sendRequest = async (query: string): Promise<String> => {
    try {
        const response = await apiClient.post('/graphdb/query', {"query": query.trim()});
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const exportToCsv = async (filename: string) => {
    try {
        const response = await apiClient.get('/graphdb/csvExport', {params: {fileName: filename}});
    } catch (error) {
        throw error;
    }
}