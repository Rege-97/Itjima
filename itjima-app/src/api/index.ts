import AsyncStorage from "@react-native-async-storage/async-storage";
import axios from "axios";
import { refreshApi } from "./auth";

const API_BASE_URL = "http://172.30.1.60:8080/api/";

// 인증이 필요 없는 API 호출용
export const publicApi = axios.create({
  baseURL: API_BASE_URL,
});

// 인증이 필요한 API 호출용
export const privateApi = axios.create({
  baseURL: API_BASE_URL,
});

// 요청 인터셉터 설정
privateApi.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem("authToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

privateApi.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const { data } = await refreshApi();
        const { accessToken } = data.data;
        await AsyncStorage.setItem("authToken", accessToken);
        privateApi.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
        return privateApi(originalRequest);
      } catch (error) {
        await AsyncStorage.multiRemove(['authToken', 'refreshToken']);
        console.error("토큰 갱신에 실패했습니다:", error);
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);