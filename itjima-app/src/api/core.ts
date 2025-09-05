import AsyncStorage from "@react-native-async-storage/async-storage";
import axios from "axios";
import { API_BASE_URL } from "@env";

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
    (config.headers as any).Authorization = `Bearer ${token}`;
  }
  return config;
});
