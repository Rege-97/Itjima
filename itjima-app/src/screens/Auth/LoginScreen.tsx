import React, { useState } from "react";
import { Button, Text, TextInput } from "react-native-paper";
import {
  Alert,
  Keyboard,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { loginApi } from "../../utils/auth";
import { useAuth } from "../../contexts/AuthContext";

const LoginScreen = ({ navigation }: any) => {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);

  const handleLogin = async () => {
    if (isLoading) {
      return;
    }

    setIsLoading(true);

    try {
      await login({ email, password });
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message || "로그인 중 오류가 발생했습니다.";
      if (errorMessage.includes("이메일 인증이 필요합니다")) {
        Alert.alert(
          "인증 필요",
          "이메일 인증을 완료해야 로그인할 수 있습니다. 인증 화면으로 이동합니다.",
          [
            {
              text: "확인",
              onPress: () => navigation.navigate("VerifyEmail", { email, password }),
            },
          ]
        );
      } else {
        Alert.alert("로그인 실패", errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
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
          textContentType="emailAddress"
          left={<TextInput.Icon icon="email-outline" />}
        />
        <TextInput
          label="비밀번호"
          value={password}
          onChangeText={setPassword}
          textContentType="password"
          secureTextEntry={!showPw}
          left={<TextInput.Icon icon="lock-outline" />}
          right={
            <TextInput.Icon
              icon={showPw ? "eye-off-outline" : "eye-outline"}
              onPress={() => setShowPw((v) => !v)}
              forceTextInputFocus={false}
            />
          }
        />
        <Button mode="contained" onPress={handleLogin} style={styles.button}>
          로그인
        </Button>
        <Button
          mode="text"
          onPress={() => navigation.navigate("RegisterWelcome")}
          style={styles.button}
        >
          회원가입
        </Button>
      </View>
    </TouchableWithoutFeedback>
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
