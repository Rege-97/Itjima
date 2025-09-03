import AsyncStorage from "@react-native-async-storage/async-storage";
import { refreshApi } from "./auth";
import { privateApi } from "./core";


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
         originalRequest.headers.Authorization = `Bearer ${accessToken}`;
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