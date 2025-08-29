import React, { useState } from "react";
import { Button, Text, TextInput } from "react-native-paper";
import { Alert, StyleSheet, View } from "react-native";
import { loginApi } from "../utils/auth";
import { useAuth } from "../contexts/AuthContext";

const LoginScreen = () => {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async () => {
    if (isLoading) {
      return;
    }

    setIsLoading(true);

    try {
      await login({ email, password });
    } catch (error: any) {
      console.error("로그인 실패", error.response?.data || error.message);
      const message =
        error.response?.data?.message || "로그인 중 오류가 발생했습니다.";
      Alert.alert("로그인 실패", message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text variant="headlineMedium" style={styles.title}>
        로그인
      </Text>
      <TextInput
        label="이메일"
        value={email}
        onChangeText={setEmail}
        style={styles.input}
        keyboardType="email-address"
        autoCapitalize="none"
      />
      <TextInput
        label="비밀번호"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <Button mode="contained" onPress={handleLogin} style={styles.button}>
        로그인
      </Button>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: "center",
  },
  title: {
    textAlign: "center",
    marginBottom: 24,
  },
  input: {
    textAlign: "center",
    marginBottom: 16,
  },
  button: {
    margin: 20,
  },
});

export default LoginScreen;
