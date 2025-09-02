import React, { useState } from "react";
import {
  Alert,
  Keyboard,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { Button, Text, TextInput } from "react-native-paper";
import {
  loginApi,
  resendVerificationEmailApi,
  verifyEmailApi,
} from "../../api/auth";
import { useAuth } from "../../contexts/AuthContext";

const VerifyEmailScreen = ({ route, navigation }: any) => {
  const { login } = useAuth();
  const { email, password } = route.params;
  const [token, setToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);

  const handleVerify = async () => {
    if (!token) {
      Alert.alert("오류", "인증번호를 입력해주세요.");
      return;
    }
    if (isLoading) {
      return;
    }
    setIsLoading(true);

    try {
      await verifyEmailApi(token);

      if (password) {
        Alert.alert("인증 성공", "자동으로 로그인합니다.");
        await login({ email, password });
      } else {
        Alert.alert(
          "인증 성공",
          "이메일 인증이 완료되었습니다. 로그인 페이지로 이동합니다.",
          [{ text: "확인", onPress: () => navigation.navigate("Login") }]
        );
      }
    } catch (error: any) {
      const message =
        error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";
      Alert.alert("인증 실패", message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (isResending) return;
    setIsResending(true);
    try {
      await resendVerificationEmailApi(email);
      Alert.alert(
        "재전송 완료",
        "인증번호를 다시 발송했습니다. 이메일을 확인해주세요."
      );
    } catch (error: any) {
      const message =
        error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";
      Alert.alert("재전송 실패", message);
    } finally {
      setIsResending(false);
    }
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <View style={styles.container}>
        <Text variant="headlineSmall" style={styles.title}>
          이메일 인증
        </Text>
        <Text style={styles.subtitle}>
          {email} 주소로 발송된 6자리 인증번호를 5분 안에 입력해주세요.
        </Text>
        <TextInput
          label="인증번호"
          value={token}
          onChangeText={setToken}
          maxLength={6}
          style={styles.input}
        />
        <Button
          mode="contained"
          loading={isLoading}
          disabled={isLoading}
          onPress={handleVerify}
          style={styles.button}
        >
          인증 확인
        </Button>
        <Button
          mode="text"
          onPress={handleResend}
          style={styles.button}
          loading={isResending}
          disabled={isLoading || isResending}
        >
          인증번호 재전송
        </Button>
      </View>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: "#fff",
    flex: 1,
    padding: 20,
    justifyContent: "center",
  },
  title: {
    textAlign: "center",
    marginBottom: 8,
  },
  subtitle: {
    textAlign: "center",
    marginBottom: 24,
  },
  input: {
    marginBottom: 16,
    textAlign: "center",
    height: 40,
    backgroundColor: "transparent",
  },
  button: {
    marginTop: 8,
  },
});

export default VerifyEmailScreen;
