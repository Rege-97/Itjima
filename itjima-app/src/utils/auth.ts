import axios from "axios";

const API_BASE_URL = 'http://172.30.1.55:8080/api/auth';

const apiClient = axios.create({
    baseURL:API_BASE_URL,
});

export const loginApi = (params:any) =>{
    return apiClient.post('/login',params);
};