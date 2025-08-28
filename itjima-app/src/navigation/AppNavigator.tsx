import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import { View } from "react-native";
import { Text } from 'react-native-paper'; 
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
  const isLoggedIn = false;

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {isLoggedIn ? (
          <Stack.Screen
            name="Main"
            component={PlaceholderScreen}
            options={{ headerShown: false }}
          />
        ) : (
          <>
            <Stack.Screen
              name="Login"
              component={PlaceholderScreen}
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