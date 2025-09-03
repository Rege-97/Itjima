import AsyncStorage from "@react-native-async-storage/async-storage";
import axios from "axios";

const API_BASE_URL = "http://172.30.1.27:8080/api/";

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