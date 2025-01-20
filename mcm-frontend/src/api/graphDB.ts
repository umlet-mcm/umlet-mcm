import axios from "axios"

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * Send a query to the graph database
 * @param query the query to send
 * @return the results of the query
 */
export const sendRequest = async (query: string): Promise<Record<string, any>[]> => {
    try {
        const response = await apiClient.post('/graphdb/query', {"query": query.trim()});
        return response.data;
    } catch (error) {
        throw error;
    }
}

/**
 * Export the graph database to a csv file
 * @param filename the name of the file to export
 */
export const exportToCsv = async (filename: string) => {
    try {
        const response = await apiClient.get('/graphdb/csvExport', {params: {fileName: filename}});
        const blob = new Blob([response.data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${filename}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (error) {
        throw error;
    }
}

export const exportQueryToCsv = async (query: string, filename: string) => {
    try {
        const response = await apiClient.post('/graphdb/csvExport', {"query": query.trim()}, {params: {fileName: filename}});
        const blob = new Blob([response.data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${filename}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (error) {
        throw error;
    }
}

export const exportQueryToUxf = async (query: string, filename: string) => {
    try {
        const response = await apiClient.post('/graphdb/queryExport', {"query": query.trim()}, {params: {fileName: filename}});
        const blob = new Blob([response.data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${filename}.uxf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (error) {
        throw error;
    }
}