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
import FindEmailScreen from "../screens/Auth/FindEmailScreen";
import FindPasswordScreen from "../screens/Auth/FindPasswordScreen";
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
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="RegisterWelcome"
              component={RegisterWelcomeScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="RegisterForm"
              component={RegisterFormScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="VerifyEmail"
              component={VerifyEmailScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="KakaoLogin"
              component={KakaoLoginScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="FindEmail"
              component={FindEmailScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="FindPassword"
              component={FindPasswordScreen}
              options={{ headerShown: false }}
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
