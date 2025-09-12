import React, { useState } from "react";
import {
  Alert,
  Keyboard,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { Button, Text, TextInput, Portal, Dialog } from "react-native-paper";
import { KeyboardAwareScrollView } from "react-native-keyboard-aware-scroll-view";
import {
  findPasswordApi,
  findPasswordEmailResendApi,
  passwordResetApi,
} from "../../api/auth";

const getData = (res: any) => res?.data?.data ?? res?.data ?? res;

export default function FindPasswordScreen({ navigation }: any) {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");

  const [loading, setLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);

  const [code, setCode] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [submittingReset, setSubmittingReset] = useState(false);
  const [resending, setResending] = useState(false);

  const onRequestCode = async () => {
    if (!name.trim() || !phone.trim() || !email.trim()) {
      Alert.alert("입력 필요", "이름, 전화번호, 이메일을 모두 입력해 주세요.");
      return;
    }
    setLoading(true);
    try {
      const res = await findPasswordApi({
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim(),
      });
      getData(res);
      setDialogOpen(true);
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ??
        e?.message ??
        "인증 코드 요청에 실패했습니다.";
      Alert.alert("오류", msg);
    } finally {
      setLoading(false);
    }
  };

  const onResend = async () => {
    if (!email.trim()) return;
    setResending(true);
    try {
      await findPasswordEmailResendApi({ email: email.trim() });
      Alert.alert("전송됨", "인증 코드를 다시 보냈습니다.");
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ?? e?.message ?? "재전송에 실패했습니다.";
      Alert.alert("오류", msg);
    } finally {
      setResending(false);
    }
  };

  const onConfirmReset = async () => {
    if (!code.trim()) {
      Alert.alert("입력 필요", "이메일로 받은 인증 코드를 입력해 주세요.");
      return;
    }
    if (!password.trim() || password.length < 8) {
      Alert.alert("비밀번호", "새 비밀번호는 8자 이상이어야 합니다.");
      return;
    }
    if (password !== password2) {
      Alert.alert("비밀번호", "비밀번호가 일치하지 않습니다.");
      return;
    }
    setSubmittingReset(true);
    try {
      const res = await passwordResetApi({
        code: code.trim(),
        password: password,
      });
      getData(res);
      setDialogOpen(false);
      Alert.alert(
        "완료",
        "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.",
        [{ text: "로그인으로", onPress: () => navigation.navigate("Login") }]
      );
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ??
        e?.message ??
        "비밀번호 재설정에 실패했습니다.";
      Alert.alert("오류", msg);
    } finally {
      setSubmittingReset(false);
    }
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <KeyboardAwareScrollView contentContainerStyle={styles.container}>
        <View style={styles.box}>
          <Text style={styles.title}>비밀번호 찾기</Text>
          <TextInput
            label="이름"
            value={name}
            onChangeText={setName}
            style={styles.input}
          />
          <TextInput
            label="전화번호"
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
            inputMode="tel"
            style={styles.input}
          />
          <TextInput
            label="이메일"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            style={styles.input}
          />
          <Button
            mode="contained"
            onPress={onRequestCode}
            loading={loading}
            disabled={loading}
            style={styles.submit}
          >
            인증코드 받기
          </Button>
          <Button onPress={() => navigation.goBack()} style={styles.backBtn}>
            로그인으로 돌아가기
          </Button>
        </View>

        <Portal>
          <Dialog visible={dialogOpen} onDismiss={() => setDialogOpen(false)}>
            <Dialog.Title>이메일 인증</Dialog.Title>
            <Dialog.Content>
              <Text style={{ marginBottom: 8 }}>
                이메일로 전송된 인증 코드를 입력하고 새 비밀번호를 설정하세요.
              </Text>
              <TextInput
                label="인증 코드"
                value={code}
                onChangeText={setCode}
                style={styles.input}
              />
              <TextInput
                label="새 비밀번호 (8자 이상)"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                style={styles.input}
              />
              <TextInput
                label="새 비밀번호 확인"
                value={password2}
                onChangeText={setPassword2}
                secureTextEntry
                style={styles.input}
              />
            </Dialog.Content>
            <Dialog.Actions style={{ justifyContent: "space-between" }}>
              <Button
                onPress={onResend}
                loading={resending}
                disabled={resending}
              >
                재전송
              </Button>
              <View style={{ flexDirection: "row" }}>
                <Button onPress={() => setDialogOpen(false)}>취소</Button>
                <Button
                  mode="contained"
                  onPress={onConfirmReset}
                  loading={submittingReset}
                  disabled={submittingReset}
                  style={{ marginLeft: 8 }}
                >
                  확인
                </Button>
              </View>
            </Dialog.Actions>
          </Dialog>
        </Portal>
      </KeyboardAwareScrollView>
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: "center",
    padding: 20,
    backgroundColor: "#fff",
  },
  box: { gap: 12 },
  title: { fontSize: 22, fontWeight: "600", marginBottom: 8 },
  input: { backgroundColor: "white" },
  submit: { marginTop: 8 },
  backBtn: { marginTop: 6 },
});
