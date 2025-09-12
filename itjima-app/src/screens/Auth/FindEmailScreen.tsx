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
import { findEmailApi } from "../../api/auth";

const maskEmail = (email: string) => {
  const [id, domain] = email.split("@");
  if (!domain) return email;
  const head = id.slice(0, Math.min(3, id.length));
  return head + "*".repeat(Math.max(1, id.length - head.length)) + "@" + domain;
};

export default function FindEmailScreen({ navigation }: any) {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  const onSubmit = async () => {
    if (!name.trim() || !phone.trim()) {
      Alert.alert("입력 필요", "이름과 전화번호를 입력해 주세요.");
      return;
    }
    setLoading(true);
    try {
      const res = await findEmailApi({
        name: name.trim(),
        phone: phone.trim(),
      });
      const data: any = res.data.data;
      const email = data.maskedEmail;
      if (!email) {
        Alert.alert("결과 없음", "일치하는 계정을 찾지 못했습니다.");
        return;
      }
      setResult(String(email));
      setDialogOpen(true);
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ??
        e?.message ??
        "이메일 찾기에 실패했습니다.";
      Alert.alert("오류", msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <KeyboardAwareScrollView contentContainerStyle={styles.container}>
        <View style={styles.box}>
          <Text style={styles.title}>이메일 찾기</Text>
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
          <Button
            mode="contained"
            onPress={onSubmit}
            loading={loading}
            disabled={loading}
            style={styles.submit}
          >
            이메일 찾기
          </Button>
          <Button onPress={() => navigation.goBack()} style={styles.backBtn}>
            로그인으로 돌아가기
          </Button>
        </View>

        <Portal>
          <Dialog visible={dialogOpen} onDismiss={() => setDialogOpen(false)}>
            <Dialog.Title>찾은 이메일</Dialog.Title>
            <Dialog.Content>
              <Text>{result ? maskEmail(result) : "-"}</Text>
            </Dialog.Content>
            <Dialog.Actions>
              <Button onPress={() => setDialogOpen(false)}>확인</Button>
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
