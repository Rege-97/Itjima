import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import { View } from "react-native";
import { ActivityIndicator, Button, Text } from "react-native-paper";
import LoginScreen from "../screens/Auth/LoginScreen";
import { useAuth } from "../contexts/AuthContext";
import RegisterFormScreen from "../screens/Auth/RegisterFormScreen";
import RegisterWelcomeScreen from "../screens/Auth/RegisterWelcomeScreen";
import VerifyEmailScreen from "../screens/Auth/VerifyEmailScreen";
import KakaoLoginScreen from "../screens/Auth/KakaoLoginScreen";
import MainTabNavigator from "./MainTabNavigator";
const Stack = createNativeStackNavigator();

const AppNavigator = () => {
  const { authToken, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {authToken ? (
          <Stack.Screen
            name="Main"
            component={MainTabNavigator}
            options={{ headerShown: false }}
          />
        ) : (
          <>
            <Stack.Screen
              name="Login"
              component={LoginScreen}
              options={{ title: "로그인" }}
            />
            <Stack.Screen
              name="RegisterWelcome"
              component={RegisterWelcomeScreen}
              options={{ title: "회원가입" }}
            />
            <Stack.Screen
              name="RegisterForm"
              component={RegisterFormScreen}
              options={{ title: "정보 입력" }}
            />
            <Stack.Screen
              name="VerifyEmail"
              component={VerifyEmailScreen}
              options={{ title: "이메일 인증" }}
            />
            <Stack.Screen
              name="KakaoLogin"
              component={KakaoLoginScreen}
              options={{ title: "카카오 로그인" }} // 모달로 띄우는 것이 좋습니다.
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
