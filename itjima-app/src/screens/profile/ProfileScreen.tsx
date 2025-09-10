import React from "react";
import {
  Alert,
  Keyboard,
  Platform,
  ScrollView,
  StyleSheet,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Button,
  Dialog,
  Portal,
  Text,
  TextInput,
} from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { SafeAreaView, useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "../../contexts/AuthContext";
import { useProfileScreen } from "./hooks/useProfileScreen";

const APP_VERSION = "1.0.0";

const formatDate = (iso?: string) => {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour12: false,
  });
};

export default function ProfileScreen() {
  const { logout } = useAuth();
  const insets = useSafeAreaInsets();

  const {
    me,
    loading,
    err,
    fetchMe,
    phoneOpen,
    pwOpen,
    confirmOpen,
    openPhoneModal,
    dismissPhoneModal,
    openPwModal,
    dismissPwModal,
    dismissConfirmModal,
    handlePhoneBackdrop,
    handlePwBackdrop,
    phone,
    setPhone,
    savingPhone,
    currentPw,
    setCurrentPw,
    newPw,
    setNewPw,
    secureCurrent,
    setSecureCurrent,
    secureNew,
    setSecureNew,
    savingPw,
    deleting,
    isKbOpen,
    onSavePhone,
    onChangePassword,
    onDeleteAccount,
    getErrMsg,
    isKakao,
    setConfirmOpen
  } = useProfileScreen();

  if (loading) {
    return (
      <SafeAreaView style={styles.center}>
        <ActivityIndicator size="large" />
        <Text>불러오는 중...</Text>
      </SafeAreaView>
    );
  }
  if (err) {
    return (
      <SafeAreaView style={styles.center}>
        <Text>{err}</Text>
        <Button mode="contained" style={{ marginTop: 12 }} onPress={fetchMe}>
          다시 시도
        </Button>
      </SafeAreaView>
    );
  }
  if (!me) return null;

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#fff" }}>
      <View
        style={{ flex: 1 }}
        onStartShouldSetResponderCapture={() => {
          if (isKbOpen) Keyboard.dismiss();
          return false;
        }}
      >
        <View style={styles.headerRow}>
          <Text variant="titleLarge" style={styles.headerTitle}>
            프로필
          </Text>
        </View>

        <ScrollView
          style={{ flex: 1 }}
          contentContainerStyle={[
            styles.container,
            { paddingBottom: 16 + insets.bottom },
          ]}
          keyboardShouldPersistTaps="always"
          keyboardDismissMode={Platform.OS === "ios" ? "on-drag" : "none"}
          onScrollBeginDrag={Keyboard.dismiss}
          removeClippedSubviews
        >
          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <MaterialCommunityIcons name="account-circle" size={20} color="#374151" />
              <Text style={styles.cardTitle}>계정 정보</Text>
            </View>

            <View style={styles.infoRow}>
              <View style={styles.infoLeft}>
                <MaterialCommunityIcons name="account" size={16} color="#6b7280" />
                <Text style={[styles.infoLabel, { color: "#6b7280" }]}>이름</Text>
              </View>
              <View style={{ flexDirection: "row", alignItems: "center", gap: 6 }}>
                {isKakao && (
                  <View style={styles.kakaoBadge}>
                    <Text style={styles.kakaoBadgeText}>KAKAO</Text>
                  </View>
                )}
                <Text style={styles.infoValue}>{me.name}</Text>
              </View>
            </View>

            <InfoRow icon="email-outline" label="이메일" value={me.email} />
            <InfoRow icon="phone" label="전화번호" value={me.phone || "-"} />
            <InfoRow icon="calendar" label="가입일" value={formatDate(me.createdAt)} />
          </View>

          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <MaterialCommunityIcons name="account-edit" size={20} color="#374151" />
              <Text style={styles.cardTitle}>정보 수정</Text>
            </View>

            <View style={styles.actionsRow}>
              <Button
                mode="contained"
                style={styles.actionBtn}
                icon="cellphone"
                onPress={openPhoneModal}
              >
                전화번호 변경
              </Button>

              {me.provider === "LOCAL" && (
                <Button
                  mode="contained"
                  style={styles.actionBtn}
                  icon="lock-reset"
                  onPress={openPwModal}
                >
                  비밀번호 변경
                </Button>
              )}
            </View>
          </View>

          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <MaterialCommunityIcons name="information-outline" size={20} color="#374151" />
              <Text style={styles.cardTitle}>앱 정보</Text>
            </View>
            <InfoRow icon="cellphone-information" label="버전" value={`v${APP_VERSION}`} />
          </View>

          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <MaterialCommunityIcons name="cog-outline" size={20} color="#374151" />
              <Text style={styles.cardTitle}>계정</Text>
            </View>

            <View style={{ gap: 10 }}>
              <Button mode="outlined" icon="logout" onPress={logout}>
                로그아웃
              </Button>
              <Button
                mode="contained"
                buttonColor="#DC2626"
                icon="trash-can-outline"
                onPress={() => setConfirmOpen(true)}
              >
                계정 탈퇴
              </Button>
            </View>
          </View>
        </ScrollView>

        <Portal>
          <Dialog
            visible={phoneOpen}
            onDismiss={handlePhoneBackdrop}
            style={styles.dialog}
            dismissable
          >
            <Dialog.Title>
              <View style={styles.dialogTitleRow}>
                <MaterialCommunityIcons name="cellphone" size={18} color="#111827" />
                <Text style={styles.dialogTitleText}>전화번호 변경</Text>
              </View>
            </Dialog.Title>

            <Dialog.ScrollArea>
              <ScrollView
                contentContainerStyle={styles.dialogContent}
                keyboardShouldPersistTaps="always"
                keyboardDismissMode={Platform.OS === "ios" ? "on-drag" : "none"}
                onScrollBeginDrag={Keyboard.dismiss}
              >
                <TextInput
                  label="전화번호"
                  value={phone}
                  onChangeText={setPhone}
                  keyboardType={Platform.OS === "ios" ? "number-pad" : "phone-pad"}
                  left={<TextInput.Icon icon="cellphone" />}
                  style={{ backgroundColor: "#fff" }}
                  returnKeyType="done"
                  onSubmitEditing={async () => {
                    try {
                      await onSavePhone();
                      Alert.alert("완료", "연락처가 업데이트되었습니다.");
                    } catch (e) {
                      Alert.alert("오류", getErrMsg(e));
                    }
                  }}
                  blurOnSubmit
                />
              </ScrollView>
            </Dialog.ScrollArea>

            <Dialog.Actions style={styles.dialogActions}>
              <Button onPress={dismissPhoneModal} textColor="#111827">
                취소
              </Button>
              <Button
                onPress={async () => {
                  try {
                    await onSavePhone();
                    Alert.alert("완료", "연락처가 업데이트되었습니다.");
                  } catch (e) {
                    Alert.alert("오류", getErrMsg(e));
                  }
                }}
                loading={savingPhone}
                disabled={savingPhone}
              >
                저장
              </Button>
            </Dialog.Actions>
          </Dialog>

          <Dialog
            visible={pwOpen}
            onDismiss={handlePwBackdrop}
            style={styles.dialog}
            dismissable
          >
            <Dialog.Title>
              <View style={styles.dialogTitleRow}>
                <MaterialCommunityIcons name="shield-key-outline" size={18} color="#111827" />
                <Text style={styles.dialogTitleText}>비밀번호 변경</Text>
              </View>
            </Dialog.Title>

            <Dialog.ScrollArea>
              <ScrollView
                contentContainerStyle={styles.dialogContent}
                keyboardShouldPersistTaps="always"
                keyboardDismissMode={Platform.OS === "ios" ? "on-drag" : "none"}
                onScrollBeginDrag={Keyboard.dismiss}
              >
                <TextInput
                  label="현재 비밀번호"
                  value={currentPw}
                  onChangeText={setCurrentPw}
                  secureTextEntry={secureCurrent}
                  left={<TextInput.Icon icon="lock-outline" />}
                  right={
                    <TextInput.Icon
                      icon={secureCurrent ? "eye-off-outline" : "eye-outline"}
                      onPress={() => setSecureCurrent((v) => !v)}
                    />
                  }
                  style={{ backgroundColor: "#fff", marginBottom: 8 }}
                  returnKeyType="next"
                />
                <TextInput
                  label="새 비밀번호"
                  value={newPw}
                  onChangeText={setNewPw}
                  secureTextEntry={secureNew}
                  left={<TextInput.Icon icon="lock-reset" />}
                  right={
                    <TextInput.Icon
                      icon={secureNew ? "eye-off-outline" : "eye-outline"}
                      onPress={() => setSecureNew((v) => !v)}
                    />
                  }
                  style={{ backgroundColor: "#fff" }}
                  returnKeyType="done"
                  onSubmitEditing={async () => {
                    try {
                      await onChangePassword();
                      Alert.alert("완료", "비밀번호가 변경되었습니다.");
                    } catch (e) {
                      Alert.alert("오류", getErrMsg(e));
                    }
                  }}
                  blurOnSubmit
                />
              </ScrollView>
            </Dialog.ScrollArea>

            <Dialog.Actions style={styles.dialogActions}>
              <Button onPress={dismissPwModal} textColor="#111827">
                취소
              </Button>
              <Button
                onPress={async () => {
                  try {
                    await onChangePassword();
                    Alert.alert("완료", "비밀번호가 변경되었습니다.");
                  } catch (e) {
                    Alert.alert("오류", getErrMsg(e));
                  }
                }}
                loading={savingPw}
                disabled={savingPw || !currentPw || !newPw}
              >
                변경
              </Button>
            </Dialog.Actions>
          </Dialog>

          <Dialog
            visible={confirmOpen}
            onDismiss={dismissConfirmModal}
            style={styles.dialog}
            dismissable
          >
            <Dialog.Title>정말 삭제하시겠어요?</Dialog.Title>
            <Dialog.Content>
              <Text>계정이 영구적으로 삭제됩니다.</Text>
            </Dialog.Content>
            <Dialog.Actions>
              <Button onPress={dismissConfirmModal} disabled={deleting}>
                취소
              </Button>
              <Button
                onPress={async () => {
                  try {
                    await onDeleteAccount();
                    Alert.alert("탈퇴 완료", "계정이 삭제되었습니다.");
                  } catch (e) {
                    Alert.alert("오류", getErrMsg(e));
                  }
                }}
                loading={deleting}
                textColor="#DC2626"
              >
                삭제
              </Button>
            </Dialog.Actions>
          </Dialog>
        </Portal>
      </View>
    </SafeAreaView>
  );
}

function InfoRow({
  icon,
  label,
  value,
}: {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  label: string;
  value?: string | number | null;
}) {
  return (
    <View style={styles.infoRow}>
      <View style={styles.infoLeft}>
        <MaterialCommunityIcons name={icon} size={16} color="#6b7280" />
        <Text style={[styles.infoLabel, { color: "#6b7280" }]}>{label}</Text>
      </View>
      <Text style={styles.infoValue}>{value ?? "-"}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  headerRow: {
    paddingHorizontal: 16,
    paddingTop: 4,
    paddingBottom: 8,
    backgroundColor: "#fff",
  },
  headerTitle: {
    fontWeight: "700",
  },
  container: {
    padding: 16,
    gap: 14,
  },
  card: {
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#FFFFFF",
    padding: 14,
    gap: 10,
  },
  cardHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    marginBottom: 2,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: "700",
    color: "#111827",
  },
  actionsRow: {
    flexDirection: "row",
    gap: 10,
  },
  actionBtn: {
    flex: 1,
  },
  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 12,
    paddingVertical: 6,
  },
  infoLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  infoLabel: {
    fontSize: 13,
  },
  infoValue: {
    fontSize: 13,
    color: "#111827",
    fontWeight: "600",
  },
  kakaoBadge: {
    backgroundColor: "#FEE500",
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: "#F5D000",
  },
  kakaoBadgeText: {
    fontSize: 10,
    fontWeight: "900",
    color: "#3C1E1E",
    includeFontPadding: false,
  },
  badge: {
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: "700",
  },
  dangerText: {
    color: "#7F1D1D",
    marginBottom: 6,
    fontSize: 12,
  },
  dialog: {
    backgroundColor: "#ffffff",
    borderRadius: 16,
    paddingBottom: 6,
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 6,
  },
  dialogTitleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  dialogTitleText: {
    fontWeight: "700",
    color: "#111827",
    fontSize: 18,
  },
  dialogContent: {
    backgroundColor: "#ffffff",
    paddingTop: 8,
    paddingHorizontal: 4,
  },
  dialogActions: {
    justifyContent: "flex-end",
    paddingHorizontal: 12,
  },
  center: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#fff",
  },
});
