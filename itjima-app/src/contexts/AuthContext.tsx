import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  Children,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import { loginApi, kakaoLoginApi } from "../api/auth";

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
    const loadToken = async () => {
      try {
        const token = await AsyncStorage.getItem("authToken");
        if (token) {
          setAuthToken(token);
        }
      } catch (error) {
        console.error("저장소에서 토큰을 불러오는 데 실패했습니다", error);
      } finally {
        setIsLoading(false);
      }
    };
    loadToken();
  }, []);

  const login = async (params: any) => {
    const response = await loginApi(params);
    const { accessToken } = response.data.data;
    setAuthToken(accessToken);
    await AsyncStorage.setItem("authToken", accessToken);
  };

  const logout = async () => {
    setAuthToken(null);
    await AsyncStorage.removeItem("authToken");
  };

  const kakaoLoginWithCode = async (code: string) => {
    const serverResponse = await kakaoLoginApi(code);
    const { accessToken } = serverResponse.data.data;
    setAuthToken(accessToken);
    await AsyncStorage.setItem("authToken", accessToken);
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
