import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import { View } from "react-native";
import { ActivityIndicator, Text } from "react-native-paper";
import LoginScreen from "../screens/LoginScreen";
import { useAuth } from "../contexts/AuthContext";
const Stack = createNativeStackNavigator();

const PlaceholderScreen = ({ route }: any) => {
  const { name } = route;
  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <Text variant="headlineLarge">{name} Screen</Text>
    </View>
  );
};

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
            component={PlaceholderScreen}
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
              name="Register"
              component={PlaceholderScreen}
              options={{ title: "회원가입" }}
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
