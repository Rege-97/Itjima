import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import { loginApi, kakaoLoginApi } from "../api/auth";
import "../api/index";
import { privateApi } from "../api/core";
import { setLogoutHandler } from "../utils/session";


interface AuthContextType {
  authToken: string | null;
  isLoading: boolean;
  login: (params: any) => Promise<void>;
  logout: () => void;
  kakaoLoginWithCode: (code: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth는 반드시 AuthProvider 안에서 사용되어야 합니다.");
  }
  return context;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [authToken, setAuthToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const token = await AsyncStorage.getItem("authToken");
        if (token) {
          setAuthToken(token);
          (privateApi.defaults.headers as any).Authorization = `Bearer ${token}`;
        }
      } finally {
        setIsLoading(false);
      }
    })();

    setLogoutHandler(async () => {
      await logout();
    });
  }, []);

  const login = async (params: any) => {
    const response = await loginApi(params);
    const { accessToken, refreshToken } = response.data.data;
    setAuthToken(accessToken);
    await AsyncStorage.setItem("authToken", accessToken);
    await AsyncStorage.setItem("refreshToken", refreshToken);
  };

  const logout = async () => {
    setAuthToken(null);
    await AsyncStorage.removeItem("authToken");
    await AsyncStorage.removeItem("refreshToken");
  };

  const kakaoLoginWithCode = async (code: string) => {
    const serverResponse = await kakaoLoginApi(code);
    const { accessToken, refreshToken } = serverResponse.data.data;
    setAuthToken(accessToken);
    await AsyncStorage.setItem("authToken", accessToken);
    await AsyncStorage.setItem("refreshToken", refreshToken);
  };

  const value = {
    authToken,
    isLoading,
    login,
    logout,
    kakaoLoginWithCode,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
