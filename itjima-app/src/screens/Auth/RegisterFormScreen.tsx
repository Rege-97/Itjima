import React, { use, useState } from "react";
import {
  Alert,
  Keyboard,
  ScrollView,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { Button, Text, TextInput } from "react-native-paper";
import { registerApi } from "../../api/auth";
import { KeyboardAwareScrollView } from "react-native-keyboard-aware-scroll-view";

const RegisterFormScreen = ({ navigation }: any) => {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [showCpw, setShowCpw] = useState(false);
  const [phone, setPhone] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleRegister = async () => {
    if (password !== confirmPassword) {
      Alert.alert("오류", "비밀번호가 일치하지 않습니다.");
      return;
    }

    if (isLoading) {
      return;
    }
    setIsLoading(true);

    try {
      await registerApi({ name, email, password, phone });
      Alert.alert(
        "회원가입 성공",
        "가입하신 이메일로 인증번호를 발송했습니다. 다음 화면에서 인증을 완료해주세요.",
        [
          {
            text: "확인",
            onPress: () => navigation.navigate("VerifyEmail", { email }),
          },
        ]
      );
    } catch (error: any) {
      const message =
        error.response?.data?.message || "알 수 없는 오류가 발생했습니다.";
      Alert.alert("회원가입 실패", message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <KeyboardAwareScrollView
        contentContainerStyle={styles.container}
        enableOnAndroid
        keyboardShouldPersistTaps="handled"
      >
        <Text variant="headlineMedium" style={styles.title}>
          회원가입
        </Text>
        <TextInput
          label="이름을 입력해주세요"
          value={name}
          left={<TextInput.Icon icon="account-outline" />}
          onChangeText={setName}
          textContentType="name"
          style={styles.input}
        />
        <TextInput
          label="email@example.com"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
          textContentType="emailAddress"
          left={<TextInput.Icon icon="email-outline" />}
          style={styles.input}
        />
        <TextInput
          label="비밀번호를 입력해주세요"
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
          style={styles.input}
        />
        <TextInput
          label="비밀번호 확인"
          value={confirmPassword}
          onChangeText={setConfirmPassword}
          textContentType="password"
          left={<TextInput.Icon icon="lock-outline" />}
          secureTextEntry={!showCpw}
          right={
            <TextInput.Icon
              icon={showCpw ? "eye-off-outline" : "eye-outline"}
              onPress={() => setShowCpw((v) => !v)}
              forceTextInputFocus={false}
            />
          }
          style={styles.input}
        />
        <TextInput
          label="휴대폰 번호를 -없이 입력해주세요"
          value={phone}
          onChangeText={setPhone}
          textContentType="telephoneNumber"
          keyboardType="phone-pad"
          left={<TextInput.Icon icon="cellphone" />}
          style={styles.input}
        />
        <Button
          mode="contained"
          onPress={handleRegister}
          loading={isLoading}
          disabled={isLoading}
          style={styles.button}
        >
          가입하기
        </Button>
      </KeyboardAwareScrollView>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: "#fff", 
    flexGrow: 1,
    padding: 20,
    justifyContent: "center",
  },
  title: {
    textAlign: "center",
    marginBottom: 24,
  },
  input: {
    marginBottom: 16,
        height: 40,
    backgroundColor: "transparent",
  },
  button: {
    marginTop: 8,
  },
});
export default RegisterFormScreen;
