import AsyncStorage from "@react-native-async-storage/async-storage";
import { refreshApi } from "./auth";
import { privateApi } from "./core";
import { triggerLogout } from "../utils/session";

let isRefreshing = false;
let pendingQueue: Array<(t?: string) => void> = [];

privateApi.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (!error.response) {
      return Promise.reject(error);
    }

    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push((token?: string) => {
            if (token) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            privateApi(originalRequest).then(resolve).catch(reject);
          });
        });
      }

      isRefreshing = true;
      try {
        const { data } = await refreshApi();
        const { accessToken } = data.data;

        await AsyncStorage.setItem("authToken", accessToken);
        privateApi.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        pendingQueue.forEach((cb) => cb(accessToken));
        pendingQueue = [];
        return privateApi(originalRequest);
      } catch (error) {
        await AsyncStorage.multiRemove(["authToken", "refreshToken"]);
        pendingQueue.forEach((cb) => cb());
        pendingQueue = [];
        triggerLogout();
        console.error("토큰 갱신에 실패했습니다:", error);
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(error);
  }
);
