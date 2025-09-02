import AsyncStorage from "@react-native-async-storage/async-storage";
import { publicApi } from ".";

export const loginApi = (params: any) => {
  return publicApi.post("/auth/login", params);
};

export const registerApi = (params: any) => {
  return publicApi.post("/auth/signup", params);
};

export const verifyEmailApi = (token: string) => {
  return publicApi.get(`/auth/verify-email?token=${token}`);
};

export const resendVerificationEmailApi = (email: string) => {
  return publicApi.post("/auth/verify-email/resend", { email });
};

export const kakaoLoginApi = (code: string) => {
  return publicApi.get(`/auth/kakao?code=${code}`);
};

export const refreshApi = async () => {
  const refreshToken = await AsyncStorage.getItem("refreshToken");
  if(!refreshToken){
    return Promise.reject("No refresh token available");
  }
  return publicApi.post("/auth/refresh", { refreshToken });
};
