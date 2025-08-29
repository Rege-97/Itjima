import React from "react";
import { PaperProvider } from "react-native-paper";
import AppNavigator from "./src/navigation/AppNavigator";
import { AuthProvider } from "./src/contexts/AuthContext";

export default function App() {
  return (
    <AuthProvider>
      <PaperProvider>
        <AppNavigator />
      </PaperProvider>
    </AuthProvider>
  );
}
