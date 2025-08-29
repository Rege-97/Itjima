import React, { useState } from "react";
import { Button, Text, TextInput } from "react-native-paper";
import {
  Alert,
  Image,
  Keyboard,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { loginApi } from "../../utils/auth";
import { useAuth } from "../../contexts/AuthContext";
import { KeyboardAwareScrollView } from "react-native-keyboard-aware-scroll-view";

const LoginScreen = ({ navigation }: any) => {
  const { login, kakaoLoginWithCode } = useAuth();
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
              onPress: () =>
                navigation.navigate("VerifyEmail", { email, password }),
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
      <KeyboardAwareScrollView
        contentContainerStyle={styles.container}
        enableOnAndroid
        keyboardShouldPersistTaps="handled"
      >
        <Text variant="headlineMedium" style={styles.title}>
          잊지마
        </Text>
        <Text style={styles.label}>이메일 주소</Text>
        <TextInput
          value={email}
          onChangeText={setEmail}
          style={styles.input}
          keyboardType="email-address"
          autoCapitalize="none"
          textContentType="emailAddress"
          placeholder="예) itjima@itjima.co.kr"
          placeholderTextColor="#888"
        />
        <Text style={styles.label}>비밀번호</Text>
        <TextInput
          value={password}
          onChangeText={setPassword}
          style={styles.input}
          textContentType="password"
          secureTextEntry={!showPw}
          right={
            <TextInput.Icon
              icon={showPw ? "eye-off-outline" : "eye-outline"}
              onPress={() => setShowPw((v) => !v)}
              forceTextInputFocus={false}
            />
          }
        />
        <Button
          mode="contained"
          onPress={handleLogin}
          style={styles.button}
          labelStyle={{ fontSize: 18 }}
        >
          로그인
        </Button>
        <TouchableOpacity
          onPress={() => navigation.navigate("KakaoLogin")}
          style={styles.kakaoButton}
        >
          <Image
            source={require("../../assets/images/kakao_login_large_wide.png")}
            style={styles.kakaoImage}
          />
        </TouchableOpacity>

        <View style={styles.textContainer}>
          <TouchableOpacity
            onPress={() => navigation.navigate("RegisterWelcome")}
          >
            <Text style={styles.text}>이메일 가입</Text>
          </TouchableOpacity>
          <View style={styles.divider} />
          <TouchableOpacity>
            <Text style={styles.text}>이메일 찾기</Text>
          </TouchableOpacity>
          <View style={styles.divider} />
          <TouchableOpacity>
            <Text style={styles.text}>비밀번호 찾기</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAwareScrollView>
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
    marginBottom: 24,
  },
  label: {
    marginLeft: 15,
    marginTop: 30,
    fontWeight: "bold",
  },
  input: {
    marginLeft: 15,
    marginRight: 15,
    height: 40,
    backgroundColor: "transparent",
  },
  button: {
    marginLeft: 15,
    marginRight: 15,
    marginTop: 25,
    borderRadius: 30,
    height: 50,
    justifyContent: "center",
  },
  kakaoButton: {
    marginLeft: 15,
    marginRight: 15,
    marginTop: 10,
    borderRadius: 30,
     overflow: "hidden"
  },
  kakaoImage: {
    width: "100%",
    height: 50,
    resizeMode: "cover",
  },
  textContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
    marginTop: 20,
    paddingLeft: 30,
    paddingRight: 30,
  },
  text: {
    fontSize: 13,
    color: "#555",
  },
  divider: {
    width: 1,
    height: 12,
    backgroundColor: "#ccc", // 세로 구분선
    marginHorizontal: 8,
  },
});

export default LoginScreen;
