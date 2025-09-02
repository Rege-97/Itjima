import { publicApi } from ".";

export const loginApi = (params: any) => {
  return publicApi.post("/login", params);
};

export const registerApi = (params: any) => {
  return publicApi.post("/signup", params);
};

export const verifyEmailApi = (token: string) => {
  return publicApi.get(`/verify-email?token=${token}`);
};

export const resendVerificationEmailApi = (email: string) => {
  return publicApi.post("/verify-email/resend", { email });
};

export const kakaoLoginApi = (code: string) => {
  return publicApi.get(`/kakao?code=${code}`);
};
