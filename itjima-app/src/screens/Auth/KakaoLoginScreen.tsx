// src/screens/Auth/KakaoLoginScreen.tsx

import React from "react";
import { WebView } from "react-native-webview";
import { useAuth } from "../../contexts/AuthContext";
import { Alert } from "react-native";

const KakaoLoginScreen = ({ navigation }: any) => {
  const { kakaoLoginWithCode } = useAuth();

  const REST_API_KEY = "5f28982a2024f1d2fc549f6f01896693";
  const REDIRECT_URI = "https://auth.expo.io/@rege/itjima-app/kakao-auth";

  const authorizationUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code`;

  const handleNavigationStateChange = (navState: any) => {
    const { url } = navState;

    const decodedUrl = decodeURIComponent(url);

    if (decodedUrl.startsWith(REDIRECT_URI)) {
      const code = new URL(url).searchParams.get("code");
    
      if (code) {
        navigation.goBack();
        kakaoLoginWithCode(code).catch((error) => {
          Alert.alert(
            "카카오 로그인 실패",
            "로그인 처리 중 문제가 발생했습니다."
          );
        });
      } else {
        // 코드를 얻지 못했다면 로그인 창을 닫음
        navigation.goBack();
      }
    }
  };

  return (
    <WebView
      source={{ uri: authorizationUrl }}
      onNavigationStateChange={handleNavigationStateChange}
    />
  );
};

export default KakaoLoginScreen;
