import axios from "axios"

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'multipart/form-data'
    }
});

export const uploadUxf = async (change: any) => {
    try {
        let formData = new FormData();
        formData.append("file", change.target.files[0]);
        const response = await apiClient.post(
            '/files/uxf',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return response.status;
    } catch (error) {
        throw error;
    }
};