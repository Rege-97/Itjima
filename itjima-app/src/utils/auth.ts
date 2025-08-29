import axios from "axios";

const API_BASE_URL = "http://172.30.1.55:8080/api/auth";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

export const loginApi = (params: any) => {
  return apiClient.post("/login", params);
};

export const registerApi = (params: any) => {
  return apiClient.post("/signup", params);
};

export const verifyEmailApi = (token : string) =>{
    return apiClient.get(`/verify-email?token=${token}`);
}

export const resendVerificationEmailApi = (email: string) => {
  return apiClient.post('/verify-email/resend', { email });
};
